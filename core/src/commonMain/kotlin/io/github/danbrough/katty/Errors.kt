package io.github.danbrough.katty

import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

sealed class KattyError(override val message: String) : Exception(message) {
  class MissingContextException(override val message: String, val cls: KClass<*>) :
    KattyError(message)

  class ExitException : KattyError("Exit Requested")

  class CommandNotFound(val cmdName: String) :
    KattyError("Command $cmdName not found")


  class InvalidCommandArgs(override val message: String) : KattyError(message)

  companion object {
    fun errorInvalidCommandUsage(message: String): Nothing = throw InvalidCommandArgs(message)

    fun errorExit(): Nothing = throw ExitException()

    inline fun <reified C : CoroutineContext> errorMissingContext(message: String = "Missing context: ${C::class.simpleName}"): Nothing =
      throw MissingContextException(message, C::class)
  }
}