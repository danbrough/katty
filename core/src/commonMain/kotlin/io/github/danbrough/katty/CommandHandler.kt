package io.github.danbrough.katty

fun interface CommandHandler {

  fun runCommand(kTerminal: KTerminal, args: List<String>)
}

