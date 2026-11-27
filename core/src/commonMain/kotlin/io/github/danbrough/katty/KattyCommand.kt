package io.github.danbrough.katty

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext


open class KattyCommandContext<A>(val shell: KattyShell, open val input: A) : KattyShell by shell,
  CoroutineContext.Element {
  companion object : CoroutineContext.Key<KattyCommandContext<*>>
}

suspend fun kattyCommandContext(): KattyCommandContext<List<String>> =
  kattyCommandContext<List<String>>()

@Suppress("UNCHECKED_CAST")
suspend inline fun <T> kattyCommandContext(): KattyCommandContext<T> =
  currentCoroutineContext()[KattyCommandContext] as? KattyCommandContext<T>
    ?: KattyError.errorMissingContext(KattyCommandContext)


open class KattyCommand<A, R>(
  open var helpText: String? = null,
  open var command: (suspend KattyCommandContext<A>.() -> R)? = null
) {

  open suspend fun initialize() = Unit
  open suspend fun createContext(shell: KattyShell, input: A): KattyCommandContext<A> =
    KattyCommandContext(shell, input)

  open suspend operator fun invoke(shell: KattyShell, input: A): R =
    command?.invoke(createContext(shell, input)) ?: error("No command specified")
}

