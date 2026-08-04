package io.github.danbrough.katty

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.input.isCtrlC
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.io.SystemLineSeparator

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

      while (true) {
        val prompt = searchPrompt(searchTerm)
        val searchPromptLength = prompt.length
        var match: String? = null

        terminal.cursor.move {
          startOfLine()
          clearLineAfterCursor()
        }
        terminal.rawPrint(prompt)

        if (searchTerm.isNotBlank()) {
          kTerminal.history.history.firstOrNull {
            it.contains(searchTerm)
          }?.also { result ->
            match = result
            terminal.rawPrint(
              result.replace(
                searchTerm,
                TextStyles.inverse(searchTerm)
              )
            )
          }
        }
        val firstKey = terminal.enterRawMode().use { raw ->
          raw.readKeyOrNull()!!
        }
        //println(firstKey)

        if (firstKey.key == "Backspace" && searchTerm.isNotEmpty()) {
          searchTerm = searchTerm.substring(0, searchTerm.length - 1)
          terminal.cursor.move {
            startOfLine()
            right(searchPromptLength)
            clearLineAfterCursor()
          }
          continue
        }

        if (firstKey.key == "Escape") {
          break
        }

        if (firstKey.key == "Enter" && match != null) {
          kTerminal.cursorPos = 0
          kTerminal.currentLine.clear()
          terminal.cursor.move {
            startOfLine()
            clearLine()
          }
          kTerminal.runCommand(match)
          return KeyboardActionResult.CONTINUE
        }

        if (firstKey.key.length == 1) {
          searchTerm += firstKey.key
        }
      }


      kTerminal.cursorPos = 0
      kTerminal.currentLine.clear()
      terminal.cursor.move {
        startOfLine()
        clearLine()
      }
      //terminal.println()
      return KeyboardActionResult.CONTINUE
    }
  }


  val Enter = KeyboardAction({ key == "Enter" }) {
    if (currentLine.isNotBlank()) {
      val cmdLine = currentLine.toString().also {
        currentLine.clear()
      }
      history.addToHistory(cmdLine)
      runCommand(cmdLine)

    } else {
      terminal.rawPrint("$SystemLineSeparator${prompt()}")
      cursorPos = promptLength
    }
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
      kTerminal.run {
        if (currentLine.isBlank()) return KeyboardActionResult.CONTINUE
        var index = cursorPos - promptLength
        if (index == 0) return KeyboardActionResult.CONTINUE

        fun currentCharIsWhitespace(): Boolean =
          index < currentLine.length && currentLine[index].isWhitespace()

        fun previousCharIsWhitespace(): Boolean = index > 0 && currentLine[index - 1].isWhitespace()
        fun previousCharIsNotWhitespace(): Boolean =
          index > 0 && !currentLine[index - 1].isWhitespace()

        val restOfLine = if (index < currentLine.length) currentLine.substring(index) else ""

        if (!previousCharIsWhitespace()) {
          //example:  "123 45[6]7" =>  "123 [6]7"  ([6] means "cursor at character "6")

          terminal.cursor.move {
            while (previousCharIsNotWhitespace()) {
              left(1)
              cursorPos--
              index--
              currentLine.deleteAt(index)
            }
            clearLineAfterCursor()
          }

          terminal.rawPrint(restOfLine)
          terminal.cursor.move {
            left(restOfLine.length)
          }
          return KeyboardActionResult.CONTINUE
        }

        //else previousCharIsWhitespace
        //example "123 [4]567" =>  "[4]567"  ([4] means "cursor at character 4)
        terminal.cursor.move {
          while (previousCharIsWhitespace()) {
            left(1)
            cursorPos--
            index--
            currentLine.deleteAt(index)
          }
          while (previousCharIsNotWhitespace()) {
            left(1)
            cursorPos--
            index--
            currentLine.deleteAt(index)
          }
          clearLineAfterCursor()
        }
        terminal.rawPrint(restOfLine)
        terminal.cursor.move {
          left(restOfLine.length)
        }
      }

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


