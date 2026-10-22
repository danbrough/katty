package io.github.danbrough.katty

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.updateAndFetch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class MutableContext(initialContext: CoroutineContext = EmptyCoroutineContext) : CoroutineContext {
  private val contextRef: AtomicReference<CoroutineContext> = AtomicReference(initialContext)

  override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? =
    contextRef.load()[key]

  override fun <R> fold(
    initial: R,
    operation: (R, CoroutineContext.Element) -> R
  ): R = contextRef.load().fold(initial, operation)

  override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext =
    contextRef.load().minusKey(key)

  override fun plus(context: CoroutineContext): CoroutineContext = contextRef.load().plus(context)

  fun addToContext(element: CoroutineContext.Element): CoroutineContext =
    contextRef.updateAndFetch {
      it.plus(element)
    }

  fun removeFromContext(key: CoroutineContext.Key<*>): CoroutineContext =
    contextRef.updateAndFetch {
      it[key]?.let { e ->
        if (e is AutoCloseable) e.close()
        it.minusKey(key)
      } ?: it
    }
}