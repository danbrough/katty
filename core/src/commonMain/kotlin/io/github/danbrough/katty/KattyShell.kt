package io.github.danbrough.katty

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext


interface KattyShell : KattyContext {

  val currentLine: StringBuilder
  val linePos: Int
  var cursorPos: Int
  val promptLength: Int
  fun warn(message: String)
  fun muted(message: String)
  fun danger(message: String)
  fun success(message: String)
  fun info(message: String)
  fun print(message: String)
  fun println(message: String)
  fun println()

  suspend fun printPrompt(newLine: Boolean = true)

  suspend fun runCommand(args: List<String>, singleCommandRun: Boolean = false)
}

object KattyShellKey : CoroutineContext.Key<KattyShell>

suspend fun kattyShellOrNull(): KattyShell? = currentCoroutineContext()[KattyShellKey]
suspend fun kattyShell(): KattyShell = kattyShellOrNull() ?: error("No KattyShell in context")

abstract class AbstractKattyShell() : KattyShell {

  override val key: CoroutineContext.Key<*> = KattyShellKey

  override var cursorPos: Int = 0
  override var promptLength: Int = 0

  override val currentLine: StringBuilder = StringBuilder()

  override val linePos: Int
    get() = cursorPos - promptLength

  protected open val welcomeMessage: String = "Welcome to Katty"


}