package io.github.danbrough.katty

object Errors {


  abstract class KattyException(override val message: String) : Exception()

  class CommandNotFound(val cmdName: String) : KattyException("Command not found $cmdName")
}