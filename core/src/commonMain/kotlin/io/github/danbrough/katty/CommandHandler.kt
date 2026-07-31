package io.github.danbrough.katty

interface CommandHandler {
  fun matches(args: List<String>): Boolean
  fun addCompletion(completions: MutableList<String>)
  fun exec(kTerminal: KTerminal, args: List<String>)
}