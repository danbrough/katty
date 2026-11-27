package io.github.danbrough.katty

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


open class KattyCommandContext<A>(val shell: KattyShell, open val input: A) :
  CoroutineContext.Element, KattyShell by shell {
  companion object : CoroutineContext.Key<KattyCommandContext<*>>

  override val key: CoroutineContext.Key<*> = KattyCommandContext
}

suspend fun kattyCommandContextDefault(): BasicCommandContext = kattyCommandContext()
suspend fun kattyCommandContextDefaultOrNull(): BasicCommandContext? =
  kattyCommandContextOrNull()


suspend inline fun <T> kattyCommandContext(): KattyCommandContext<T> =
  kattyCommandContextOrNull<T>() ?: KattyError.errorMissingContext(KattyCommandContext)

@Suppress("UNCHECKED_CAST")
suspend inline fun <T> kattyCommandContextOrNull(): KattyCommandContext<T>? =
  currentCoroutineContext()[KattyCommandContext] as? KattyCommandContext<T>


open class KattyCommand<A, R>(
  open var helpText: String? = null,
  open var command: (suspend KattyCommandContext<A>.() -> R)? = null
) {

  open suspend fun initialize() = Unit
  open suspend fun createContext(shell: KattyShell, input: A): KattyCommandContext<A> =
    KattyCommandContext(shell, input)

  open suspend operator fun invoke(context: KattyCommandContext<A>): R = withContext(context) {
    command?.invoke(context) ?: error("No command specified")
  }
}

typealias BasicCommandContext = KattyCommandContext<List<String>>
typealias BasicCommandJob = suspend BasicCommandContext.() -> Unit
typealias BasicCommand = KattyCommand<List<String>, Unit>


fun basicCommand(cmdName: String, helpText: String, job: BasicCommandJob) =
  cmdName to BasicCommand(helpText, job)