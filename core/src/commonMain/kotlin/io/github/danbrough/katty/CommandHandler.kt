package io.github.danbrough.katty

interface CommandHandler {
  fun matches(args: List<String>): Boolean = false
  fun addCompletion(completions: MutableList<String>) = Unit
  fun exec(kTerminal: KTerminal, args: List<String>)
  fun helpText(): String? = null

}