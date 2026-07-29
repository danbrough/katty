package io.github.danbrough.katty

object CommandHelp : CommandHandler {
  override fun matches(cmdLine: String): Boolean = cmdLine.matches("^\\s*help[\\s+]".toRegex())

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(kTerminal: KTerminal, cmdLine: String) = kTerminal.run {
    terminal.println("Available commands:")
    commandHandlers.forEach { cmd->
      terminal.println(cmd)
    }
  }
}


object CommandRegex : CommandHandler {
  override fun matches(cmdLine: String): Boolean = cmdLine.matches("^\\s*regex(\\s+.*)?".toRegex())

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(kTerminal: KTerminal, cmdLine: String) = kTerminal.run {
    terminal.println()
    terminal.println("Regex match $cmdLine")
    val args = cmdLine.substringAfter("regex ").split("\\s+".toRegex())
    if (args.size == 1) return@run
    args.forEach {
      terminal.println("ARG [$it]")
    }
  }
}