package io.github.danbrough.katty

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.input.isCtrlC

enum class KeyboardActionResult {
  CONTINUE, EXIT, ADD_TO_LINE
}

open class KeyboardAction(
  val matcher: KeyboardEvent.() -> Boolean,
  private val action: KTerminal.(KeyboardEvent) -> KeyboardActionResult = { KeyboardActionResult.CONTINUE }
) {
  open operator fun invoke(kTerminal: KTerminal, event: KeyboardEvent): KeyboardActionResult =
    action(kTerminal, event)
}

object KeyboardActions {
  val CtrlDCtrlCToExit = KeyboardAction({ isCtrlD || isCtrlC }) {
    KeyboardActionResult.EXIT
  }

  object SearchAction : KeyboardAction({ isCtrlR }) {
    override fun invoke(kTerminal: KTerminal, event: KeyboardEvent): KeyboardActionResult {
      val terminal = kTerminal.terminal
      val searchPrompt: (String) -> String = {
        "search `$it`: "
      }

      var searchTerm = ""
      var prompt = searchPrompt(searchTerm)
      var searchPromptLength = prompt.length

      while (true) {
        terminal.cursor.move {
          startOfLine()
          clearLineAfterCursor()
        }
        terminal.rawPrint(prompt)
        val firstKey = terminal.enterRawMode().use { raw ->
          raw.readKeyOrNull()
        } ?: continue

        if (firstKey.key == "Escape") {
          break
        }

        if (firstKey.key.length == 1) {
          searchTerm += firstKey.key
        }
      }

      return KeyboardActionResult.CONTINUE
    }
  }


  val Enter = KeyboardAction({ key == "Enter" }) {
    cursorPos = 0
    if (currentLine.isNotBlank()) {
      history.addToHistory(currentLine.toString())
      runCommand(currentLine.toString())
    }
    terminal.println()
    cursorPos = 0
    currentLine.clear()
    KeyboardActionResult.CONTINUE
  }

  val LeftArrow = KeyboardAction({ key == "ArrowLeft" }) {
    if (cursorPos > promptLength) {
      cursorPos--
      terminal.cursor.move {
        left(1)
      }
    }
    KeyboardActionResult.CONTINUE
  }

  val RightArrow = KeyboardAction({ key == "ArrowRight" }) {
    if (cursorPos < currentLine.length + promptLength) {
      cursorPos++
      terminal.cursor.move {
        right(1)
      }
    }
    KeyboardActionResult.CONTINUE
  }

  val Backspace = KeyboardAction({ key == "Backspace" }) {
    if (cursorPos > promptLength) {
      cursorPos--
      currentLine.deleteAt(cursorPos - promptLength)
      val restOfLine = currentLine.substring(cursorPos - promptLength)

      terminal.cursor.move {
        left(1)
        clearLineAfterCursor()
      }
      terminal.rawPrint(restOfLine)
      terminal.cursor.move {
        left(restOfLine.length)
      }
    }
    KeyboardActionResult.CONTINUE
  }

  val Home = KeyboardAction({ key == "Home" || isCtrl("a") }) {
    cursorPos = promptLength
    terminal.cursor.move {
      startOfLine()
      right(cursorPos)
    }
    KeyboardActionResult.CONTINUE
  }

  object End : KeyboardAction({ key == "End" || isCtrl("e") }) {
    override fun invoke(kTerminal: KTerminal, event: KeyboardEvent): KeyboardActionResult {
      kTerminal.run {
        cursorPos = promptLength + currentLine.length
        terminal.cursor.move {
          startOfLine()
          right(cursorPos)
        }
      }
      return KeyboardActionResult.CONTINUE
    }
  }

  object CtrlW : KeyboardAction({ isCtrlW }) {
    override fun invoke(kTerminal: KTerminal, event: KeyboardEvent): KeyboardActionResult {
      return KeyboardActionResult.CONTINUE
    }
  }

  val ArrowUp = KeyboardAction({ key == "ArrowUp" }) {
    showHistory(true)
    KeyboardActionResult.CONTINUE
  }
  val ArrowDown = KeyboardAction({ key == "ArrowDown" }) {
    showHistory(false)
    KeyboardActionResult.CONTINUE
  }
  val DefaultActions =
    listOf(
      CtrlDCtrlCToExit,
      SearchAction,
      Enter,
      LeftArrow,
      RightArrow,
      Backspace,
      Home,
      End,
      ArrowUp,
      ArrowDown,
      CtrlW,
    )
}


fun KeyboardEvent.isCtrl(keyCode: String): Boolean = key == keyCode && ctrl && !alt && !shift

val KeyboardEvent.isCtrlD: Boolean
  get() = isCtrl("d")

val KeyboardEvent.isCtrlR: Boolean
  get() = isCtrl("r")

val KeyboardEvent.isCtrlW: Boolean
  get() = isCtrl("w")


