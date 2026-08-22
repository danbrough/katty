package io.github.danbrough.katty

fun interface CommandHandler {
  suspend fun runCommand(kTerminal: KTerminal, args: List<String>)

  suspend fun showHelp(kTerminal: KTerminal) = Unit
  suspend fun tabPressed(terminal: KTerminal) = Unit
}

