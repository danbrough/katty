package io.github.danbrough.katty

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext


open class KattyCommandContext<A>(shell: KattyShell, open val arg: A) : KattyShell by shell,
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
  val shell: KattyShell,
  open var help: String? = null,
  open var command: (suspend KattyCommandContext<A>.() -> R)? = null
) {

  open suspend fun initialize() = Unit
  open suspend fun createContext(a: A): KattyCommandContext<A> = KattyCommandContext(shell, a)

  open suspend operator fun invoke(a: A): R =
    command?.invoke(createContext(a)) ?: error("No command specified")
}

