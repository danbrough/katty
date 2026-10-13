package io.github.danbrough.katty

import kotlin.reflect.KClass


object Errors {

  fun errorInvalidCommandUsage(message: String): Nothing = throw KattyError.InvalidCommandArgs(
    message
  )

  inline fun <reified C : KattyContext> errorMissingContext(message: String = "Missing context: ${C::class.simpleName}"): Nothing =
    throw KattyError.MissingContextException(message, C::class)
}

sealed class KattyError(override val message: String) : Exception(message) {
  class MissingContextException(override val message: String, val cls: KClass<*>) :
    KattyError(message)

  class ExitException : KattyError("Exit Requested")

  class CommandNotFound(val cmdName: String) :
    KattyError("Command $cmdName not found")

  class InvalidCommandArgs(override val message: String) : KattyError(message)
}