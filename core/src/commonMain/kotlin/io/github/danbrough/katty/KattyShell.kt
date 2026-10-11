package io.github.danbrough.katty

import kotlin.coroutines.CoroutineContext

abstract class KattyShell() : CoroutineContext.Element {

  var cursorPos: Int = 0
  var promptLength: Int = 0

  var currentLine: StringBuilder = StringBuilder()
  val linePos: Int
    get() = cursorPos - promptLength

  abstract fun warn(message: String)
  abstract fun muted(message: String)
  abstract fun danger(message: String)
  abstract fun success(message: String)
  abstract fun info(message: String)
  abstract fun print(message: String)
  abstract fun println(message: String)
  abstract fun println()

  abstract suspend fun printPrompt(newLine: Boolean = true)

  abstract suspend fun runCommand(args: List<String>, singleCommandRun: Boolean = false)
}