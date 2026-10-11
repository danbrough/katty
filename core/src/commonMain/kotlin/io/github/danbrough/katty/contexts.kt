package io.github.danbrough.katty

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface KattyContext : CoroutineContext.Element, CoroutineContext.Key<KattyContext>,
  AutoCloseable


abstract class ShellContext : KattyContext {
  companion object : CoroutineContext.Key<ShellContext> {

    suspend fun add(element: CoroutineContext.Element) {
      shellContext().context += element
    }

    suspend fun remove(key: CoroutineContext.Key<*>) {
      val shellContext = shellContext()
      shellContext.context[key]?.also { e ->
        shellContext.context = shellContext.context.minusKey(key)
        if (e is AutoCloseable) e.close()
      }
    }
  }

  override val key: CoroutineContext.Key<*> = ShellContext

  protected var context: CoroutineContext = this

/*

  operator fun plusAssign(element: CoroutineContext.Element) {
    context[element.key]?.also { item ->
      if (item is AutoCloseable) item.close()
    }
    context += element
  }

  operator fun minusAssign(key: CoroutineContext.Key<*>) {
    context[key]?.also { item ->
      if (item is AutoCloseable) item.close()
    }
    context = context.minusKey(key)
  }

*/

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
  currentCoroutineContext()[ShellContext] ?: Errors.errorMissingContext<ShellContext>()


/*suspend fun <E : CoroutineContext.Element> shellContext(key: CoroutineContext.Key<E>): E =
  currentCoroutineContext()[ShellContext]?.context[key]
    ?: Errors.errorMissingContext<ShellContext>()*/


class CommandContext(val cmd: List<String>) : KattyContext {
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

  override fun close() {
  }
}


