package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextStyles

open class HelpCommand : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull().let {
    it == "help" || it == "-h" || it == "?"
  }

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(
    kTerminal: KTerminal,
    args: List<String>
  ) {
    kTerminal.commandHandlers.forEach {
      kTerminal.terminal.println(TextStyles.bold(it.helpText()))
    }

  }

  override fun helpText(): String = "help: Prints the help for the available commands"
}