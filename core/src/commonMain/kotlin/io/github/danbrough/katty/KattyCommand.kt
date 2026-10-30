package io.github.danbrough.katty

import io.github.danbrough.katty.KattyCommand.Context


abstract class KattyCommand<C : Context, R>(
  protected val command: (suspend C.() -> R)? = null
) {

  open class Context(
    shell: KattyShell, val args: List<String>
  ) : KattyShell by shell

  open val help: String = "No help available"

  suspend fun run(shell: KattyShell, args: List<String>): R = invoke(context(shell, args))

  abstract suspend fun context(shell: KattyShell, args: List<String>): C

  open suspend operator fun invoke(context: C): R =
    command?.invoke(context) ?: error("No command specified")
}

open class KattyCommandBasic<C : Context>(
  val contextBuilder: (KattyShell, List<String>) -> C,
  command: suspend C.() -> Unit
) : KattyCommand<C, Unit>(command) {
  override suspend fun context(shell: KattyShell, args: List<String>): C =
    contextBuilder(shell, args)
}