package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles

class BasicCommandHandler : CommandHandler {
  private val commands = mutableMapOf<String, TerminalCommand>()

  fun registerCommands(vararg cmds: Pair<String, TerminalCommand>) {
    commands.putAll(cmds)
  }

  override fun showHelp(kTerminal: KTerminal) {
    commands.mapValues { it.value.helpText() }.filter { it.value != null }.forEach {
      kTerminal.println(TextColors.green(TextStyles.bold(it.key) + ":\t${it.value}"))
    }
  }

  override fun runCommand(
    kTerminal: KTerminal,
    args: List<String>
  ) {
    val cmdName = args.firstOrNull() ?: "help"
    if (cmdName == "help") return showHelp(kTerminal)

    kTerminal.run {
      if (commands.contains(cmdName)) {
        commands[cmdName]?.invoke(this, args)
      } else {
        error("Command not found: $cmdName")
      }
    }
  }
}