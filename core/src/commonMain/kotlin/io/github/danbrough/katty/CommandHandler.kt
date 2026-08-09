package io.github.danbrough.katty

interface CommandHandler {

  fun runCommand(kTerminal: KTerminal, args: List<String>)
}

