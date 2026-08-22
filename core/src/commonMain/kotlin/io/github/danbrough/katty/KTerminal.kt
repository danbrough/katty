package io.github.danbrough.katty

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.HorizontalRule
import kotlinx.io.SystemLineSeparator


open class KTerminal(
  val commandHandler: CommandHandler,
  val history: History = DefaultHistory(),
  var terminal: Terminal = Terminal(),
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

  /**
   * Return the string length of the prompt and the formatted prompt itself
   */
  var prompt: KTerminal.() -> Pair<Int, String> = {
    $$"$ ".let {
      it.length to TextStyles.bold(TextColors.brightGreen(it))
    }
  }

  val keyboardActions: MutableList<KeyboardAction> = mutableListOf()

  protected open fun registerDefaultKeyboardActions() =
    keyboardActions.addAll(KeyboardActions.DefaultActions)


  fun println(message: String = "") = print("$message$SystemLineSeparator")

  fun print(message: String) = terminal.print(message)


  open suspend fun runCommand(

    cmdLine: String? = null,
    args: List<String>? = cmdLine?.trim()?.let { parseCommandLineArgs(it) },
    printNewLine: Boolean = true
  ) {
    args ?: error("No args or cmdLine provided to runCommand()")

    if (printNewLine)
      terminal.println()
    cursorPos = 0
    currentLine.clear()

    runCatching {
      cmdLine?.also {
        history.addToHistory(it)
        history.saveHistory()
      }
      commandHandler.runCommand(this, args)
    }.exceptionOrNull()?.also {
      terminal.println(HorizontalRule())
      terminal.println(terminal.theme.danger(it.stackTraceToString()))
      terminal.println(HorizontalRule())
      commandHandler.showHelp(this)
      terminal.println(HorizontalRule())
    }
  }

  fun printPrompt(newLine: Boolean = true) {
    prompt().also { p ->
      terminal.rawPrint("${if (newLine) SystemLineSeparator else ""}${p.second}")
      promptLength = p.first
      cursorPos = promptLength
      currentLine.clear()
    }
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

          terminal.cursor.move {
            terminal.cursor.hide(true)
            clearLineAfterCursor()
            terminal.rawPrint(restOfLine)
            left(restOfLine.length - 1)
            terminal.cursor.show()
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

  open fun tabPressed(){
    commandHandler.tabPressed(this)
  }

  open fun hello() {
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

  open fun goodBye() {
    println("${SystemLineSeparator}Bye!")
  }

  suspend fun run() {
    runCatching {
      hello()
      cmdLoop()
    }.exceptionOrNull().also { err ->
      runCatching {
        history.saveHistory()
      }.exceptionOrNull()?.also {
        it.printStackTrace()
      }
      if (err == KeyboardActions.ExitException) {
        goodBye()
      } else if (err != null) throw err
    }
  }

  suspend fun main(cmdArgs: Array<String>) {
    val args = cmdArgs.toMutableList()
    val interactive = args.firstOrNull() == "-i"
    if (interactive) args.removeFirst()
    if (args.isNotEmpty())
      runCommand(args = args)
    if (interactive || args.isEmpty())
      run()
  }
}



