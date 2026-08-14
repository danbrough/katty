package io.github.danbrough.katty

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.input.isCtrlC
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.CursorMovements
import kotlinx.io.SystemLineSeparator
import kotlinx.io.files.FileNotFoundException

enum class KeyboardActionResult {
  CONTINUE, EXIT, ADD_TO_LINE
}

open class KeyboardAction(
  val matcher: KeyboardEvent.() -> Boolean,
  private val action: suspend KTerminal.(KeyboardEvent) -> Unit = { }
) {
  open suspend operator fun invoke(kTerminal: KTerminal, event: KeyboardEvent): Unit =
    action(kTerminal, event)
}

object KeyboardActions {

  val ExitException = Exception("Ctrl-C or Ctrl-D pressed")


  val CtrlDCtrlCToExit = KeyboardAction({ isCtrlD || isCtrlC }) {
    throw ExitException
  }

  val SearchAction = KeyboardAction({ isCtrlR }) {
    searchAction()
  }

  val Enter = KeyboardAction({ key == "Enter" }) {
    if (currentLine.isNotBlank()) {
      val cmdLine = currentLine.toString().also {
        currentLine.clear()
      }
      runCommand(cmdLine)
    } else {
      printPrompt(true)
    }

  }

  val LeftArrow = KeyboardAction({ key == "ArrowLeft" }) {
    if (cursorPos > promptLength) {
      cursorPos--
      terminal.cursor.move {
        left(1)
      }
    }


  }

  val RightArrow = KeyboardAction({ key == "ArrowRight" }) {
    if (cursorPos < currentLine.length + promptLength) {
      cursorPos++
      terminal.cursor.move {
        right(1)
      }
    }

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

  }

  val Home = KeyboardAction({ key == "Home" || isCtrl("a") }) {
    cursorPos = promptLength
    terminal.cursor.move {
      startOfLine()
      right(cursorPos)
    }
  }

  object End : KeyboardAction({ key == "End" || isCtrl("e") }) {
    override suspend fun invoke(kTerminal: KTerminal, event: KeyboardEvent) {
      kTerminal.run {
        cursorPos = promptLength + currentLine.length
        terminal.cursor.move {
          startOfLine()
          right(cursorPos)
        }
      }
    }
  }

  object CtrlW : KeyboardAction({ isCtrlW }) {
    override suspend fun invoke(kTerminal: KTerminal, event: KeyboardEvent) {
      kTerminal.run {
        if (currentLine.isBlank()) return
        var index = cursorPos - promptLength
        if (index == 0) return

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
          return
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

      return
    }
  }

  val ArrowUp = KeyboardAction({ key == "ArrowUp" }) {
    showHistory(true)

  }
  val ArrowDown = KeyboardAction({ key == "ArrowDown" }) {
    showHistory(false)

  }

  val CtrlArrowLeft = KeyboardAction({ isCtrl("ArrowLeft") }) {
    ctrlArrowLeft()

  }

  val CtrlArrowRight = KeyboardAction({ isCtrl("ArrowRight") }) {
    ctrlArrowRight()

  }

  val DefaultActions =
    listOf(
      CtrlDCtrlCToExit,
      CtrlArrowLeft,
      CtrlArrowRight,
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


private fun KTerminal.skipWhitespaceLeft(cursorMovements: CursorMovements): Boolean {
  var skippedAny = false
  while (linePos > 0 && (linePos >= currentLine.length || currentLine[linePos].isLetterOrDigit())) {
    skippedAny = true
    cursorPos--
    cursorMovements.left(1)
  }
  return skippedAny
}

private fun KTerminal.skipToWordStartLeft(cursorMovements: CursorMovements): Boolean {
  var skippedAny = false
  while (linePos > 1 && (!currentLine[linePos - 1].isWhitespace())) {
    skippedAny = true
    cursorPos--
    cursorMovements.left(1)
  }
  return skippedAny
}

private fun KTerminal.ctrlArrowLeft() {
  if (cursorPos <= promptLength) return
  terminal.cursor.move {
    terminal.cursor.hide(true)

    while (linePos >= currentLine.length) {
      left(1)
      cursorPos--
    }

    while (linePos > 0 && !currentLine[linePos - 1].isLetterOrDigit()) {
      left(1)
      cursorPos--
    }

    while (linePos > 0 && currentLine[linePos - 1].isLetterOrDigit()) {
      left(1)
      cursorPos--
    }

    terminal.cursor.show()
  }
}


private fun KTerminal.ctrlArrowRight() {
  if (linePos >= currentLine.length - 1) return
  terminal.cursor.move {
    terminal.cursor.hide(true)

    if (linePos < currentLine.length) {
      right(1)
      cursorPos++
    }

    while (linePos > 0 && linePos < currentLine.length) {
      if (currentLine[linePos - 1].isLetterOrDigit() && !currentLine[linePos].isLetterOrDigit()) break
      right(1)
      cursorPos++
    }

    terminal.cursor.show()
  }
}


private suspend fun KTerminal.searchAction() {

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
      history.history.firstOrNull {
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
      cursorPos = 0
      currentLine.clear()
      terminal.cursor.move {
        startOfLine()
        clearLine()
      }
      runCommand(match)
      return
    }

    if (firstKey.key.length == 1) {
      searchTerm += firstKey.key
    }
  }


  cursorPos = 0
  currentLine.clear()
  terminal.cursor.move {
    startOfLine()
    clearLine()
  }
}