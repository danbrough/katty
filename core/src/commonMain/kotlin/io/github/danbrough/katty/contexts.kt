package io.github.danbrough.katty

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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

abstract class ShellContext : KattyContext {
  companion object : CoroutineContext.Key<ShellContext> {

    suspend fun add(element: CoroutineContext.Element) {
      shellContext().add(element)
    }

    suspend fun remove(key: CoroutineContext.Key<*>) {
      shellContext().remove(key)
    }
  }

  override val key: CoroutineContext.Key<*> = ShellContext

  protected var context: CoroutineContext = CoroutineName("ShellContext") + this

  abstract val scope: CoroutineScope

  suspend fun add(element: CoroutineContext.Element) {
    context += element
  }

  suspend fun remove(key: CoroutineContext.Key<*>) {
    context[key]?.also { e ->
      context = context.minusKey(key)
      if (e is AutoCloseable) e.close()
    }
  }

  /**
   * Close the AutoClosable elements of the coroutine context
   */
  override fun close() {
    context.fold(Unit) { _, element ->
      if (element is AutoCloseable && element != this@ShellContext)
        element.close()
    }
  }
}

suspend fun shellContext(): ShellContext =
  currentCoroutineContext()[ShellContext] ?: KattyError.errorMissingContext<ShellContext>()


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


}


