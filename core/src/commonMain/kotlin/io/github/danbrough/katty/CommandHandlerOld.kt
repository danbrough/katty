package io.github.danbrough.katty

interface CommandHandlerOld {
  fun matches(args: List<String>): Boolean

  fun addCompletion(completions: MutableList<String>) = Unit

  fun exec(kTerminal: KTerminal, args: List<String>)
  fun helpText(): String?
}