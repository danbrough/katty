package io.github.danbrough.katty

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.io.SystemLineSeparator
import kotlinx.io.files.Path


open class KTerminal(val terminal: Terminal, val history: History) {
  constructor(historyFile: Path? = null) : this(
    Terminal(interactive = true),
    DefaultHistory(historyFile)
  )

  var cursorPos: Int = 0
  var promptLength: Int = 0

  var currentLine: StringBuilder = StringBuilder()

  /**
   * Must update the [promptLength] property with the character length of the prompt returned
   */
  var prompt: KTerminal.() -> String = {
    $$"$ ".let {
      promptLength = it.length
      TextStyles.bold(TextColors.brightGreen(it))
    }
  }

  val keyboardActions: MutableList<KeyboardAction> = mutableListOf()

  protected open fun registerDefaultKeyboardActions() =
    keyboardActions.addAll(KeyboardActions.DefaultActions)

  open fun runCommand(cmd: String) {
    terminal.rawPrint("${SystemLineSeparator}running command: <$cmd>")
  }

  fun cmdLoop() {

    registerDefaultKeyboardActions()

    while (true) {
      if (cursorPos == 0) {
        terminal.rawPrint(prompt())
        cursorPos = promptLength
      }

      val firstKey = terminal.enterRawMode().use { raw ->
        raw.readKeyOrNull()
      } ?: continue


      val actionResult =
        keyboardActions.firstOrNull { it.matcher(firstKey) }?.invoke(this, firstKey)
      when (actionResult) {
        KeyboardActionResult.EXIT -> {
          terminal.println()
          return
        }

        KeyboardActionResult.CONTINUE -> continue
        KeyboardActionResult.ADD_TO_LINE, null -> {}
      }

      if (!firstKey.ctrl && !firstKey.alt && firstKey.key.length == 1) {
        val c = firstKey.key.first()
        currentLine.insert(cursorPos - promptLength, c)
        terminal.rawPrint(currentLine.substring(cursorPos - promptLength))
        cursorPos++
        terminal.cursor.move {
          startOfLine()
          right(cursorPos)
        }
      } else {
        handleUnknownKey(firstKey)
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
      left(cursorPos - promptLength)
      clearLineAfterCursor()
    }
    terminal.rawPrint(line)
    cursorPos = line.length + promptLength
    currentLine.clear().insert(0, line)
  }

  fun run() {
    history.loadHistory()
    runCatching {
      cmdLoop()
    }.exceptionOrNull().also { err ->
      runCatching {
        history.saveHistory()
      }.exceptionOrNull()?.also {
        it.printStackTrace()
      }
      if (err != null) throw err
    }
  }
}



