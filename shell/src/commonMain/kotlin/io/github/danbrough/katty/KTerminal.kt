package io.github.danbrough.katty

import com.github.ajalt.mordant.input.InputReceiver
import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.RawModeScope
import com.github.ajalt.mordant.input.coroutines.receiveKeyEventsFlow
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.HorizontalRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.io.SystemLineSeparator
import kotlin.coroutines.CoroutineContext

private val log = logKattyShell

open class KTerminal(
  var commandHandler: CommandHandler,
  val history: History = DefaultHistory(),
  val terminal: Terminal = Terminal(),
  val executor: CommandExecutor = CommandExecutor()
) : KattyShell {

  init {
    history.loadHistory()
  }

  companion object : CoroutineContext.Key<KTerminal>

  override val key: CoroutineContext.Key<*> = KTerminal
  var cursorPos: Int = 0
  var promptLength: Int = 0

  var currentLine: StringBuilder = StringBuilder()

  val linePos: Int
    get() = cursorPos - promptLength

  val keyboardActions: MutableList<KeyboardAction> = mutableListOf<KeyboardAction>().also {
    it.addAll(KeyboardActions.DefaultActions)
  }

  fun println(message: String = "", style: TextStyle = terminal.theme.info) =
    print("$message$SystemLineSeparator", style).also {
      cursorPos = 0
    }

  override fun warn(message: String) = println(message, terminal.theme.warning)
  override fun muted(message: String) = println(message, terminal.theme.muted)
  override fun danger(message: String) = println(message, terminal.theme.danger)
  override fun success(message: String) = println(message, terminal.theme.success)

  override fun info(message: String) = println(message, terminal.theme.info)

  fun print(message: String, style: TextStyle = terminal.theme.info) {
    cursorPos += message.length
    terminal.print(style(message))
  }

  open suspend fun runCommand(cmdLine: String) = history.addToHistory(cmdLine).also {
    runCommand(commandHandler.parseCommandLine(cmdLine))
  }

  override suspend fun runCommand(args: List<String>, singleCommandRun: Boolean) {
    executor.execute(args) {
      cursorPos = 0
      currentLine.clear()

      runCatching {
        commandHandler.runCommand(this@KTerminal, args)
      }.exceptionOrNull().also {
        if (it == null) {
          if (!singleCommandRun)
            printPrompt(false)
        } else {
          if (it is CancellationException) {
            log.trace { "runCommand::caught a CancellationException" }
            return@execute
          }

          terminal.println(HorizontalRule())
          if (it is Errors.CommandNotFound)
            terminal.println(terminal.theme.danger(it.message!!))
          else
            terminal.println(terminal.theme.danger(it.stackTraceToString()))

          terminal.println(HorizontalRule())
          commandHandler.showHelp(this@KTerminal)
          terminal.println(HorizontalRule())
        }
      }
    }
  }


  suspend fun printPrompt(newLine: Boolean = true) {
    commandHandler.prompt().also { p ->
      terminal.rawPrint("${if (newLine) SystemLineSeparator else ""}${p.second}")
      promptLength = p.first
      cursorPos = promptLength
      currentLine.clear()
    }
  }

  protected suspend fun processKeyEvent(event: KeyboardEvent): InputReceiver.Status<Any> {
    if (cursorPos == 0)
      printPrompt(newLine = false)

    keyboardActions.firstOrNull { it.matcher(event) }?.also {
      it.invoke(this@KTerminal, event)
      return@processKeyEvent InputReceiver.Status.Continue
    }

    if (!event.ctrl && !event.alt && event.key.length == 1) {
      val c = event.key.first()
      currentLine.insert(cursorPos - promptLength, c)
      cursorPos++
      val restOfLine = currentLine.substring(cursorPos - promptLength - 1)

      if (restOfLine.length == 1) {
        //terminal.print(restOfLine)
        terminal.rawPrint(terminal.theme.info(restOfLine))
      } else {
        terminal.cursor.move {
          terminal.cursor.hide(true)
          clearLineAfterCursor()
          terminal.rawPrint(terminal.theme.info(restOfLine))
          left(restOfLine.length - 1)
          terminal.cursor.show()
        }
      }
    } else {
      handleUnknownKey(event)
    }

    return InputReceiver.Status.Continue
  }


  open fun showHistory(up: Boolean) {
    val line = (if (up) history.previous() else history.next()) ?: return

    terminal.cursor.move {
      terminal.cursor.hide(true)
      startOfLine()
      right(promptLength)
      clearLineAfterCursor()
    }

    terminal.rawPrint(terminal.theme.info(line))
    terminal.cursor.show()
    cursorPos = line.length + promptLength
    currentLine.clear().append(line)
  }


  open suspend fun hello() {
    terminal.println(
      Caption(
        HorizontalRule(),
        bottom = TextColors.brightGreen("Welcome to Katty"),
        bottomAlign = TextAlign.LEFT
      )
    )
    terminal.println(HorizontalRule())
    commandHandler.showHelp(this)
    terminal.println(HorizontalRule())
  }


  protected open fun handleUnknownKey(key: KeyboardEvent) {
    var prefix = if (key.ctrl) "Ctrl-" else ""
    if (key.alt) prefix += "Alt-"
    if (key.shift) prefix += "Shift-"
    terminal.rawPrint(terminal.theme.danger("${SystemLineSeparator}Unknown key: $prefix${key.key}$SystemLineSeparator"))
    println(key)
    cursorPos = 0
    currentLine.clear()
  }


  suspend fun cmdLoop() {
    log.info { "KTerminal::cmdLoop()" }
    while (true) {
      printPrompt()
      runCatching {
        terminal.receiveKeyEventsFlow().collect {
          processKeyEvent(it)
          //yield()
        }
      }.exceptionOrNull()?.also {
        if (it is Errors.ExitException) {
          log.info { "got an exit exception .. current job: ${executor.currentJob}" }
          if (executor.cancelCurrentJob()) continue
        }
        throw it
      }
    }
  }

  private lateinit var rawScope: RawModeScope

  fun readKey(): KeyboardEvent {
    if (!::rawScope.isInitialized)
      rawScope = terminal.enterRawMode()
    return rawScope.readKeyOrNull()!!
  }


  open suspend fun runLoop() = runCatching {
    log.trace { "KTerminal::runLoop()" }
    hello()

    cmdLoop()
  }.exceptionOrNull().also { err ->
    when (err) {
      is CancellationException, is Errors.ExitException -> {
        log.trace { "cancelled or exited: ${err.message}" }
      }

      null -> {}
      else -> {
        danger("Error:${err.message}")
        err.printStackTrace()
      }
    }
    onClose()
  }

  protected open suspend fun onClose() {
    runCatching {
      if (::rawScope.isInitialized) rawScope.close()
      if (history.saveHistory())
        println("History saved.")
      cursorPos = 0
    }.exceptionOrNull()?.also {
      it.printStackTrace()
    }
    executor.shutdown()
    if (cursorPos != 0) print(SystemLineSeparator)
    println("Bye!")
    cursorPos = 0
  }


  suspend fun main(cmdArgs: List<String>) {
    val args = cmdArgs.toMutableList()

    val removeArgIf: (String) -> Boolean = {
      if (args.firstOrNull() == it) {
        args.removeFirst()
        true
      } else false
    }

    val interactive = removeArgIf("-i") || args.isEmpty()
    log.trace { "interactive: $interactive args.count: ${args.size}" }

    withContext(this) {
      if (args.isNotEmpty()) {
        log.trace { "running command: $args" }
        runCommand(args = args, singleCommandRun = true)
        if (!interactive) {
          executor.shutdown()
          return@withContext
        }
      }
      runLoop()
    }
  }

  suspend fun push(commandHandler: CommandHandler) {
    this.commandHandler = commandHandler
  }
}


private suspend fun KTerminal.readCommand() {

  while (true) {
    val e = readKey()

  }
}