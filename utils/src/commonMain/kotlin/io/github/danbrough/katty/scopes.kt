package io.github.danbrough.katty

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class KattySession() :
  CoroutineContext.Element, AutoCloseable {
  companion object : CoroutineContext.Key<KattySession>

  override val key: CoroutineContext.Key<*> = KattySession

  var context: CoroutineContext = EmptyCoroutineContext

  override fun close() {
    fold(Unit) { _, element ->
      if (element is AutoCloseable) element.close()
    }
  }
}

val GLOBAL_SESSION = KattySession()

suspend fun <R> globalSession(block: (suspend () -> R)): R =
  withContext(GLOBAL_SESSION) {
    GLOBAL_SESSION.use {
      block()
    }
  }




