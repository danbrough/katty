package io.github.danbrough.katty

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext


class KattyCommandContext<A>(
  val shell: KattyShell, val arg: A, override val key: CoroutineContext.Key<*> = KattyCommandContext
) : KattyShell by shell, CoroutineContext.Element {
  companion object : CoroutineContext.Key<KattyCommandContext<*>>
}

suspend fun kattyCommandContext(): KattyCommandContext<List<String>> =
  kattyCommandContext<List<String>>()

@Suppress("UNCHECKED_CAST")
suspend inline fun <T> kattyCommandContext(): KattyCommandContext<T> =
  currentCoroutineContext()[KattyCommandContext] as? KattyCommandContext<T>
    ?: KattyError.errorMissingContext(KattyCommandContext)


abstract class KattyCommand<A, R>(
  open val help: String? = null,
  protected val command: (suspend KattyCommandContext<A>.() -> R)? = null
) {

  lateinit var context: KattyCommandContext<A>

  open suspend fun initialize() = Unit

  open suspend operator fun invoke(context: KattyCommandContext<A>): R =
    command?.invoke(context) ?: error("No command specified")
}

