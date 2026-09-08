package io.github.danbrough.katty

import com.github.ajalt.mordant.input.InputReceiver
import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.coroutines.receiveKeyEventsFlow
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.HorizontalRule
import kotlinx.coroutines.CancellationException
import kotlinx.io.SystemLineSeparator


open class KTerminal(
  var commandHandler: CommandHandler,
  val history: History = DefaultHistory(),
  var terminal: Terminal = Terminal(),
  val executor: CommandExecutor = CommandExecutor(KattyUtils.ioDispatcher),
  val context: Any? = null
) {

  init {
    history.loadHistory()
  }

  var cursorPos: Int = 0
  var promptLength: Int = 0

  var currentLine: StringBuilder = StringBuilder()

  val linePos: Int
    get() = cursorPos - promptLength


  val keyboardActions: MutableList<KeyboardAction> = mutableListOf()

  protected open fun registerDefaultKeyboardActions() =
    keyboardActions.addAll(KeyboardActions.DefaultActions)


  fun println(message: String = "") = print("$message$SystemLineSeparator")

  fun print(message: String) = terminal.print(message)


  open suspend fun runCommand(
    cmdLine: String? = null,
    args: List<String>? = null,
    printNewLine: Boolean = true
  ) {
    cmdLine ?: args ?: error("No args or cmdLine provided to runCommand()")

    println(TextColors.blue("KTermianl.runCommand: $cmdLine"))
    executor.execute {
      if (printNewLine)
        terminal.println()
      cursorPos = 0
      currentLine.clear()

      runCatching {
        cmdLine?.also {
          history.addToHistory(it)
          history.saveHistory()
        }
        commandHandler.runCommand(this@KTerminal, cmdLine, args)
      }.exceptionOrNull()?.also {
        if (it is CancellationException) throw it

        terminal.println(HorizontalRule())
        if (it is KattyException.CommandNotFound)
          terminal.println(terminal.theme.danger(it.message))
        else
          terminal.println(terminal.theme.danger(it.stackTraceToString()))

        terminal.println(HorizontalRule())
        commandHandler.showHelp(this@KTerminal)
        terminal.println(HorizontalRule())
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
        terminal.print(restOfLine)
      } else {
        terminal.cursor.move {
          terminal.cursor.hide(true)
          clearLineAfterCursor()
          terminal.rawPrint(restOfLine)
          left(restOfLine.length - 1)
          terminal.cursor.show()
        }
      }
    } else {
      handleUnknownKey(event)
    }

    return InputReceiver.Status.Continue
  }

  suspend fun cmdLoop2() {
    registerDefaultKeyboardActions()
    printPrompt()

    terminal.receiveKeyEventsFlow().collect(::processKeyEvent)
  }

  suspend fun cmdLoop() {

    registerDefaultKeyboardActions()

    terminal.println()


    terminal.enterRawMode().use { rawMode ->

      loop@ while (true) {
        if (cursorPos == 0)
          printPrompt(newLine = false)

        val firstKey = rawMode.readKeyOrNull()!!

        keyboardActions.firstOrNull { it.matcher(firstKey) }?.invoke(this, firstKey)
          ?.run { continue@loop }


        if (!firstKey.ctrl && !firstKey.alt && firstKey.key.length == 1) {
          val c = firstKey.key.first()
          currentLine.insert(cursorPos - promptLength, c)
          cursorPos++
          val restOfLine = currentLine.substring(cursorPos - promptLength - 1)

          if (restOfLine.length == 1) {
            terminal.print(restOfLine)
          } else {
            terminal.cursor.move {
              terminal.cursor.hide(true)
              clearLineAfterCursor()
              terminal.rawPrint(restOfLine)
              left(restOfLine.length - 1)
              terminal.cursor.show()
            }
          }
        } else {
          handleUnknownKey(firstKey)
        }
      }
    }
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

  open fun showHistory(up: Boolean) {
    val line = (if (up) history.previous() else history.next()) ?: return

    terminal.cursor.move {
      terminal.cursor.hide(true)
      startOfLine()
      right(promptLength)
      clearLineAfterCursor()
    }

    terminal.rawPrint(line)
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

  open suspend fun goodBye() {
    if (cursorPos != 0) print(SystemLineSeparator)
    println("Bye!")
    cursorPos = 0
  }

  suspend fun runInternal() {
    runCatching {
      cmdLoop2()
    }.exceptionOrNull().also { err ->
      if (err != null && err !is CancellationException && err !is KattyException.ExitException)
        println(this.terminal.theme.danger(err.stackTraceToString()))

      commandHandler.parent?.also {
        this.commandHandler = it
        cursorPos = 0
        currentLine.clear()
        runInternal()
      } ?: err?.also { throw it }
    }
  }


  suspend fun run() = runCatching {
    hello()
    runInternal()
  }.exceptionOrNull().also { err ->
    //if (err is CancellationException || err is KattyException.ExitException) {
    runCatching {
      if (history.saveHistory())
        println("History saved.")
      cursorPos = 0
    }.exceptionOrNull()?.also {
      it.printStackTrace()
    }
    goodBye()
  } //else if (err != null) throw err



  suspend fun main(cmdArgs: Array<String>) {
    val args = cmdArgs.toMutableList()
    val interactive = args.firstOrNull() == "-i"
    if (interactive) args.removeFirst()
    if (args.isNotEmpty())
      runCommand(args = args)
    if (interactive || args.isEmpty())
      run()
  }

  suspend fun push(commandHandler: CommandHandler) {
    this.commandHandler = commandHandler
  }
}



