package io.github.danbrough.katty

object CommandHelp : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "help"

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(kTerminal: KTerminal, args: List<String>) = kTerminal.run {
    terminal.println("Available commands:")
    commandHandlers.forEach { cmd->
      terminal.println(cmd)
    }
  }
}


object CommandRegex : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "regex"

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(kTerminal: KTerminal, args: List<String>) = kTerminal.run {
    terminal.println()
    terminal.println("Regex match $args")
    if (args.size == 1) return@run
    args.forEach {
      terminal.println("ARG [$it]")
    }
  }
}