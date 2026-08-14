package io.github.danbrough.katty

fun interface CommandHandler {

  suspend fun runCommand(kTerminal: KTerminal, args: List<String>)


  fun showHelp(kTerminal: KTerminal) = Unit
}

