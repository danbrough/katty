package io.github.danbrough.katty.demos

import io.github.danbrough.katty.logDemo
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.updateAndFetch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val log = logDemo
class ContextMessage(val message: String) : CoroutineContext.Element, AutoCloseable {
  companion object : CoroutineContext.Key<ContextMessage>

  override val key: CoroutineContext.Key<*> = ContextMessage

  override fun close() {
    log.trace { "ContextMessage::close() message:$message" }
  }

  override fun toString(): String = "ContextMessage[$message]"
}

