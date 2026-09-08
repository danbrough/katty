package io.github.danbrough.katty

sealed class KattyException(override val message: String) : Exception(message) {
  data class CommandNotFound(val cmdName: String) : KattyException("Command $cmdName not found")
  class ExitException : KattyException("Exit Requested")
}