@file:OptIn(ExperimentalAtomicApi::class)

package io.github.danbrough.katty

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext

/**
 * Where objects attached to the lifetime of the [KattyShell] are stored.
 */
open class ShellContext() : KattyContext {
  override val key: CoroutineContext.Key<*> = ShellContext

  companion object : CoroutineContext.Key<ShellContext>

  open val supervisorJob: CompletableJob = SupervisorJob()
  open val scope: CoroutineScope = CoroutineScope(supervisorJob + this)

  private val context: ConcurrentMap<CoroutineContext.Key<*>, Any> = ConcurrentMap()

  suspend fun addToContext(e: CoroutineContext.Element) {
    context.put(e.key, e)
  }

  suspend fun <E : CoroutineContext.Element> addToContext(
    key: CoroutineContext.Key<E>, provider: suspend () -> E
  ) {
    context.putOrError(key, ContextProvider(provider))
  }

  suspend fun removeFromContext(key: CoroutineContext.Key<*>) {
    context.remove(key)
  }

  @Suppress("UNCHECKED_CAST")
  suspend fun <E : CoroutineContext.Element> getOrPut(
    key: CoroutineContext.Key<E>, creator: () -> E
  ): E = context.getOrPut(key, creator) as E

  @Suppress("UNCHECKED_CAST")
  suspend fun <E : CoroutineContext.Element> getFromContextOrNull(key: CoroutineContext.Key<E>): E? =
    context[key]?.let {
      it as? E ?: (it as ContextProvider<E>).provide()
    }

  @Suppress("UNCHECKED_CAST")
  suspend inline fun <reified E : CoroutineContext.Element> getFromContext(key: CoroutineContext.Key<E>): E =
    getFromContextOrNull(key) ?: KattyError.errorMissingContext(key)

  /**
   * Close the AutoClosable elements of the coroutine context
   */
  override fun close() {
    context.clear()
  }
}

suspend fun shellContextOrNull(): ShellContext? = currentCoroutineContext()[ShellContext]

suspend inline fun shellContext(): ShellContext =
  shellContextOrNull() ?: KattyError.errorMissingContext(ShellContext)

suspend inline fun <reified E : CoroutineContext.Element> shellContext(key: CoroutineContext.Key<E>): E? =
  shellContextOrNull()?.getFromContext(key)

suspend fun <E : CoroutineContext.Element> shellContext(
  key: CoroutineContext.Key<E>, creator: () -> E
): E = shellContext().getOrPut(key, creator)

suspend fun shellContextAdd(e: CoroutineContext.Element) {
  shellContext().addToContext(e)
}

suspend fun shellContextRemove(key: CoroutineContext.Key<*>) {
  shellContext().removeFromContext(key)
}

object SingleRun : CoroutineContext.Element, CoroutineContext.Key<SingleRun> {
  override val key: CoroutineContext.Key<*> = SingleRun
}

suspend fun isSingleRun() = currentCoroutineContext()[SingleRun] != null

private class ContextProvider<T : CoroutineContext.Element>(
  val creator: suspend () -> T
) : AutoCloseable {
  private val lock = Mutex()
  private lateinit var t: T

  suspend fun provide(): T {
    if (::t.isInitialized) return t
    lock.withLock {
      if (::t.isInitialized) return t
      t = creator()
    }
    return t
  }

  override fun close() {
    if (::t.isInitialized) {
      t.also {
        if (it is AutoCloseable) it.close()
      }
    }
  }
}

suspend fun <T : CoroutineContext.Element> shellContextRegisterProvider(
  key: CoroutineContext.Key<T>, creator: suspend () -> T
) {
  shellContext().addToContext(key, creator)
}

inline fun <reified T : CoroutineContext.Element> shellContextCommand(
  cmdName: String,
  key: CoroutineContext.Key<T>,
  helpText: String = "$cmdName [start|stop]",
  noinline creator: suspend () -> T,
) = object : KattyCommand<List<String>, Unit>(helpText) {

  override suspend fun initialize() {
    shellContextRegisterProvider(key, creator)
  }

  override suspend fun invoke(shell: KattyShell, a: List<String>): Unit = shell.run {
    when (val action = a.getOrNull(1)) {
      "stop" -> shellContextRemove(key)
      null, "start" -> shellContext<T>(key)
      else -> warn("$cmdName: invalid action: $action")
    }
  }
}