package io.github.danbrough.katty

interface CommandHandler {
  fun matches(cmdLine: String): Boolean
  fun addCompletion(completions: MutableList<String>)
  fun exec(kTerminal: KTerminal, cmdLine: String)
}