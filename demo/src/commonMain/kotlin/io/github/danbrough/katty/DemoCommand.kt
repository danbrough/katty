package io.github.danbrough.katty


class DemoCommandHandler : CommandHandler {

  private val commands = mutableMapOf<String, TerminalCommand>()

  fun registerCommand(name: String, job: KTerminal.(List<String>) -> Unit) {
    commands[name] = TerminalCommand(job)
  }

  fun registerCommand(name: String, cmd: TerminalCommand) {
    commands[name] = cmd
  }

  fun registerCommands(vararg cmds: Pair<String, TerminalCommand>) {
    commands.putAll(cmds)
  }

  override fun runCommand(
    kTerminal: KTerminal,
    args: List<String>
  ) {
    val cmdName = args.firstOrNull() ?: return
    kTerminal.run {
      //println(TextColors.brightCyan("running command: $args"))
      runCatching {
        commands[cmdName]?.invoke(this, args)
          ?: println(terminal.theme.danger("Command not found: $cmdName"))
      }.exceptionOrNull()?.also {
        println(terminal.theme.danger(it.stackTraceToString()))
      }
    }
  }
}