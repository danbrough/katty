package io.github.danbrough.katty

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class CommandContext(val cmd: List<String>) : CoroutineContext.Element {
  companion object : CoroutineContext.Key<CommandContext> {

    var COMMAND_ID: Long = 1L

    suspend fun get(): CommandContext? = currentCoroutineContext()[CommandContext]

    suspend fun <C : CoroutineContext, R> withCommandContext(
      context: C,
      block: suspend C.() -> R
    ) = withContext(context) {
      if (context is AutoCloseable)
        context.use {
          it.block()
        }
      else context.block()
    }
  }

  val id: Long = COMMAND_ID++

  var job: Job? = null

  override val key: CoroutineContext.Key<*> = CommandContext
}