package io.github.danbrough.katty

import kotlin.coroutines.CoroutineContext

interface KattyShell : CoroutineContext.Element {
  fun warn(message: String)
  fun muted(message: String)
  fun danger(message: String)
  fun success(message: String)
  fun info(message: String)

  suspend fun runCommand(args: List<String>, singleCommandRun: Boolean = false)
}