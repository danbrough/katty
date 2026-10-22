package io.github.danbrough.katty

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface KattyContext : CoroutineContext.Element, AutoCloseable {
  override fun close() = Unit
}

suspend fun <E : CoroutineContext.Element> CoroutineContext.Key<E>.get(): E? =
  currentCoroutineContext()[this@get]

suspend inline fun <reified E : CoroutineContext.Element> CoroutineContext.Key<E>.getOrThrow(): E =
  currentCoroutineContext()[this@getOrThrow] ?: KattyError.errorMissingContext<E>()


fun CoroutineContext.elementList(): List<CoroutineContext> =
  fold(mutableListOf()) { acc, element ->
    acc.apply { add(element) }
  }


suspend fun <E : CoroutineContext.Element> shellContext(
  key: CoroutineContext.Key<E>,
  creator: suspend () -> E
): E = currentCoroutineContext()[key] ?: creator().also {
  ShellContext.add(it)
}


class CommandContext(val cmd: List<String>) : KattyContext {
  override val key: CoroutineContext.Key<*> = CommandContext

  companion object : CoroutineContext.Key<CommandContext> {

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

  var job: Job? = null

  override fun toString(): String =
    "Command[${cmd.joinToString(" ")}:job:$job:active:${job?.isActive}]"


}


