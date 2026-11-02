@file:OptIn(ExperimentalAtomicApi::class)

package io.github.danbrough.katty

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext

open class ShellContext() : KattyContext {
  override val key: CoroutineContext.Key<*> = ShellContext

  companion object : CoroutineContext.Key<ShellContext> {

    suspend fun add(element: CoroutineContext.Element) {
      shellContext().context.put(element.key, element)
    }

    suspend fun remove(key: CoroutineContext.Key<*>) {
      shellContext().context.remove(key)
    }
  }

  open val supervisorJob: CompletableJob = SupervisorJob()
  open val scope: CoroutineScope = CoroutineScope(supervisorJob + this)

  val context: NativeConcurrentMap<CoroutineContext.Key<*>, CoroutineContext.Element> =
    NativeConcurrentMap()


  fun addToContext(e: CoroutineContext.Element) {
    context.put(e.key, e)
  }

  fun removeFromContext(key: CoroutineContext.Key<*>) {
    context.remove(key)
  }

  /**
   * Close the AutoClosable elements of the coroutine context
   */
  override fun close() {
    fold(Unit) { _, element ->
      if (element is AutoCloseable && element != this@ShellContext)
        element.close()
    }
    context.toMap().values.forEach {
      if (it is AutoCloseable)
        it.close()
    }
  }
}

suspend fun shellContextOrNull(): ShellContext? = currentCoroutineContext()[ShellContext]

suspend fun shellContext(): ShellContext =
  shellContextOrNull() ?: KattyError.errorMissingContext<ShellContext>()