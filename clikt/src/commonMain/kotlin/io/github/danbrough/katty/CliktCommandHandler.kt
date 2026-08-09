package io.github.danbrough.katty

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.core.subcommands

class CliktCommandHandler(val bashContext: BashContext = BashContext()) : CommandHandler {

  val commands = mutableMapOf<String, () -> CliktCommand>()

  companion object{
    const val CTX_KEY_KTERMINAL = "terminal"
  }

  class RootCommand(val handler: CliktCommandHandler, val kTerminal: KTerminal,) :
    CliktCommand("katty") {

    override val allowMultipleSubcommands: Boolean = true


    init {
      context {
        obj = handler
        data[CTX_KEY_KTERMINAL] = kTerminal
        helpOptionNames = setOf("help", "-h", "--help")
      }
    }

    override fun run() {
      currentContext.data["message"] = "This is the message from the root command"
    }
  }

  override fun runCommand(
    kTerminal: KTerminal, args: List<String>
  ) {
    /*val cmd = commands[args.first()]?.invoke()
      ?: return kTerminal.println(kTerminal.terminal.theme.danger("${args.first()}: command not found"))*/

    val rootCommand = RootCommand(this, kTerminal)
    rootCommand.subcommands(commands.values.map { it.invoke() })
    val cmd = commands[args.first()]?.invoke()?.also { rootCommand.subcommands(it) }

    runCatching {
      rootCommand.parse(args)
    }.exceptionOrNull()?.also {
      if (it is CliktError) {
        (cmd ?: rootCommand).getFormattedHelp(it)?.also { err ->
          kTerminal.terminal.println(kTerminal.terminal.theme.danger(err))
        }
      } else {
        kTerminal.terminal.println(kTerminal.terminal.theme.danger("error: ${it.stackTraceToString()}"))
      }
    }

  }


}