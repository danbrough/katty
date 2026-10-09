package io.github.danbrough.katty

import kotlin.reflect.KClass


object Errors {

  class ExitException : Exception("Exit Requested")

  class CommandNotFound(val cmdName: String) :
    IllegalArgumentException("Command $cmdName not found")

  class MissingContextException(override val message: String, val cls: KClass<*>) :
    IllegalStateException(message)

  inline fun <reified C : KattyContext> errorMissingContext(message: String = "Missing context: ${C::class.simpleName}"): Nothing =
    throw MissingContextException(message, C::class)
}