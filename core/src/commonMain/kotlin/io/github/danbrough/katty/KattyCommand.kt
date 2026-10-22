package io.github.danbrough.katty

abstract class KattyCommand<C, R>(
  val commandName: String, protected val command: (suspend C.() -> R)? = null
) {
  open val help: String = "Help for $commandName"

  abstract suspend fun context(shell: KattyShell, args: List<String>): C

  suspend fun run(shell: KattyShell, args: List<String>): R = invoke(context(shell, args))

  open suspend operator fun invoke(context: C): R =
    command?.invoke(context) ?: error("No command specified")
}

