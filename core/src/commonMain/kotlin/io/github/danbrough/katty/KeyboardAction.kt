package io.github.danbrough.katty

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.isCtrlC

enum class KeyboardActionResult {
  CONTINUE, EXIT, ADD_TO_LINE
}
class KeyboardAction(
  val matcher: KeyboardEvent.() -> Boolean,
  val action: KTerminal.(KeyboardEvent) -> KeyboardActionResult
)

object KeyboardActions {
  val CtrlDCtrlCToExit = KeyboardAction({ isCtrlD || isCtrlC }) {
    KeyboardActionResult.EXIT
  }

  val Enter = KeyboardAction({ key == "Enter" }) {
    cursorPos = 0
    if (currentLine.isNotBlank()) {
      history.addToHistory(currentLine)
      runCommand(currentLine)
    }
    terminal.println()
    cursorPos = 0
    currentLine = ""
    KeyboardActionResult.CONTINUE
  }

  val LeftArrow = KeyboardAction({key == "ArrowLeft"}){
    if (cursorPos > promptLength){
      cursorPos--
      terminal.cursor.move {
        left(1)
      }
    }
    KeyboardActionResult.CONTINUE
  }

  val RightArrow = KeyboardAction({key == "ArrowRight"}){
    if (cursorPos < currentLine.length + promptLength){
      cursorPos++
      terminal.cursor.move {
        right(1)
      }
    }
    KeyboardActionResult.CONTINUE
  }

  val Backspace = KeyboardAction({key == "Backspace"}){
    if (cursorPos > promptLength){
      cursorPos--
      terminal.cursor.move {
        left(1)
        clearLineAfterCursor()
      }
      currentLine = currentLine.substring(0,currentLine.length-1)
    }
    KeyboardActionResult.CONTINUE
  }

  val Home = KeyboardAction({key == "Home"}){
    cursorPos = promptLength
    terminal.cursor.move {
      startOfLine()
      right(cursorPos)
    }
    KeyboardActionResult.CONTINUE
  }

  val End = KeyboardAction({key == "End"}){
    cursorPos = promptLength + currentLine.length
    terminal.cursor.move {
      startOfLine()
      right(cursorPos)
    }
    KeyboardActionResult.CONTINUE
  }

  val DefaultActions = listOf(CtrlDCtrlCToExit, Enter,LeftArrow,RightArrow,Backspace,Home,End)
}


val KeyboardEvent.isCtrlD: Boolean
  get() = key == "d" && ctrl && !alt && !shift

val KeyboardEvent.isCtrlR: Boolean
  get() = key == "r" && ctrl && !alt && !shift