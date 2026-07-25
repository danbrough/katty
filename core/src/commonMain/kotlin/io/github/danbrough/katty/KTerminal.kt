package io.github.danbrough.katty

import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.io.SystemLineSeparator
import kotlinx.io.files.Path


open class KTerminal(val terminal: Terminal, val history: History) {
  constructor(historyFile: Path? = null) : this(Terminal(interactive = true), DefaultHistory(historyFile))

  var cursorPos: Int = 0
  var promptLength: Int = 0

  var currentLine: String = ""

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
        keyboardActions.firstOrNull { it.matcher(firstKey) }?.action?.invoke(this, firstKey)
      when (actionResult) {
        KeyboardActionResult.EXIT -> {
          terminal.println()
          return
        }
        KeyboardActionResult.CONTINUE -> continue
        KeyboardActionResult.ADD_TO_LINE, null -> {}
      }

      if (firstKey.key.length == 1) {
        terminal.print(firstKey.key)
        currentLine += firstKey.key
        cursorPos++
      } else {
        terminal.println(terminal.theme.danger("${SystemLineSeparator}Unknown key: ${firstKey.key}"))
        cursorPos = 0
        currentLine = ""
      }
    }
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



