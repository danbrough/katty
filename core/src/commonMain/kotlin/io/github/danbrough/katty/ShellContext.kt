@file:OptIn(ExperimentalAtomicApi::class)

package io.github.danbrough.katty

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext

open class ShellContext : KattyContext {
  override val key: CoroutineContext.Key<*> = ShellContext

  init {
    println("ShellContext::CREATED")
  }

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

  private val context: ConcurrentMap<CoroutineContext.Key<*>, CoroutineContext.Element> =
    ConcurrentMap()


  suspend fun addToContext(e: CoroutineContext.Element) {
    context.put(e.key, e)
    println("context: add: ${context.values().joinToString(",")}")
  }

  suspend fun removeFromContext(key: CoroutineContext.Key<*>) {
    context.remove(key)
    println("context remove: $key: ${context.values().joinToString(",")}")
  }

  @Suppress("UNCHECKED_CAST")
  suspend fun <E : CoroutineContext.Element> getFromContext(key: CoroutineContext.Key<E>): E? =
    context[key] as? E

  /**
   * Close the AutoClosable elements of the coroutine context
   */
  override fun close() {
    println("ShellContext::close()")
    context.clear()
  }
}

suspend fun shellContextOrNull(): ShellContext? = currentCoroutineContext()[ShellContext]

suspend fun shellContext(): ShellContext =
  shellContextOrNull() ?: KattyError.errorMissingContext<ShellContext>()

suspend fun <E : CoroutineContext.Element> shellContext(key: CoroutineContext.Key<E>): E? =
  shellContextOrNull()?.getFromContext(key)
