@file:OptIn(ExperimentalAtomicApi::class)

package io.github.danbrough.katty

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext

open class ShellContext() : MutableContext(), KattyContext {
  override val key: CoroutineContext.Key<*> = ShellContext

  companion object : CoroutineContext.Key<ShellContext> {

    suspend fun add(element: CoroutineContext.Element) {
      shellContext().addToContext(element)
    }

    suspend fun remove(key: CoroutineContext.Key<*>) {
      shellContext().removeFromContext(key)
    }
  }

  open val supervisorJob: CompletableJob = SupervisorJob()
  open val scope: CoroutineScope = CoroutineScope(supervisorJob + this)


  /**
   * Close the AutoClosable elements of the coroutine context
   */
  override fun close() {
    fold(Unit) { _, element ->
      if (element is AutoCloseable && element != this@ShellContext)
        element.close()
    }
  }
}

suspend fun shellContextOrNull(): ShellContext? = currentCoroutineContext()[ShellContext]
suspend fun shellContext(): ShellContext =
  shellContextOrNull() ?: KattyError.errorMissingContext<ShellContext>()