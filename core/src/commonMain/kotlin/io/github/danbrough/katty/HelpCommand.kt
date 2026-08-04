package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.io.SystemLineSeparator

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
    buildString {
      kTerminal.commandHandlers.mapNotNull { it.helpText() }.forEach {
        append(TextStyles.bold(it))
        append(SystemLineSeparator)
      }
    }.also {
      kTerminal.terminal.rawPrint(it)
    }
    kTerminal.cursorPos = 0
  }

  override fun helpText(): String = "help: Prints the help for the available commands"
}