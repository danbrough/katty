package io.github.danbrough.katty

interface CommandHandler {
  fun completions(cmdLine: String): List<String>

  fun runCommand(kTerminal: KTerminal, args: List<String>)
}

