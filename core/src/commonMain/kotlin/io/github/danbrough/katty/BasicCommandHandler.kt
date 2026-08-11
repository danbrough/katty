package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles

open class BasicCommand(
  private val helpText: String? = null,
  private val job: (KTerminal.(List<String>) -> Unit)? = null
) {
  open operator fun invoke(kTerminal: KTerminal, args: List<String>) = job?.invoke(kTerminal, args)

  fun helpText(): String? = helpText

}


class BasicCommandHandler : CommandHandler {
  private val commands = mutableMapOf<String, BasicCommand>()

  fun registerCommands(vararg cmds: Pair<String, BasicCommand>) {
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