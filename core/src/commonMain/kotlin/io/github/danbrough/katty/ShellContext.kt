@file:OptIn(ExperimentalAtomicApi::class)

package io.github.danbrough.katty

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
import kotlin.coroutines.CoroutineContext

open class ShellContext(parentJob: Job? = null) : KattyContext {
  companion object : CoroutineContext.Key<ShellContext> {

    suspend fun add(element: CoroutineContext.Element) {
      shellContext().add(element)
    }

    suspend fun remove(key: CoroutineContext.Key<*>) {
      shellContext().remove(key)
    }
  }


  private val _context = AtomicReference(CoroutineName("ShellContext") + this)
  open val supervisorJob: CompletableJob = SupervisorJob(parentJob)

  open val scope: CoroutineScope = CoroutineScope(supervisorJob)
  override val key: CoroutineContext.Key<*> = ShellContext

  val context: CoroutineContext
    get() = _context.load()

  fun add(element: CoroutineContext.Element) {
    _context.store(_context.load() + element)
  }

  fun remove(key: CoroutineContext.Key<*>) {
    _context.update { ctx ->
      ctx[key]?.let { e ->
        if (e is AutoCloseable) e.close()
        ctx.minusKey(key)
      } ?: ctx
    }
  }

  /**
   * Close the AutoClosable elements of the coroutine context
   */
  override fun close() {
    _context.load().fold(Unit) { _, element ->
      if (element is AutoCloseable && element != this@ShellContext)
        element.close()
    }
  }
}

suspend fun shellContextOrNull(): ShellContext? = currentCoroutineContext()[ShellContext]
suspend fun shellContext(): ShellContext =
  shellContextOrNull() ?: KattyError.errorMissingContext<ShellContext>()