package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles


typealias BasicCommandJob = suspend KTerminal.(List<String>) -> Unit

fun basicCommand(
  cmdName: String,
  helpText: String,
  job: BasicCommandJob
) = cmdName to BasicCommand(helpText, job)


fun basicCommand(
  helpText: String,
  job: BasicCommandJob
) = BasicCommand(helpText, job)

open class BasicCommand(
  private val helpText: String? = null,
  private val job: BasicCommandJob? = null
) {


  /**
   * Provide a description about this command
   */
  fun helpText(): String? = helpText

  /**
   * Invoke this command
   */
  open suspend operator fun invoke(kTerminal: KTerminal, args: List<String>) =
    job?.invoke(kTerminal, args)
}


open class BasicCommandHandler(override val parent: CommandHandler? = null) : CommandHandler {

  override suspend fun prompt(): Pair<Int, String> = "$ ".let {
    it.length to TextStyles.bold(TextColors.brightGreen(it))
  }

  private val commands = mutableMapOf<String, BasicCommand>()

  fun registerCommands(vararg cmds: Pair<String, BasicCommand>) {
    commands.putAll(cmds)
  }

  operator fun set(name: String, description: String, job: BasicCommandJob) {
    registerCommands(name to BasicCommand(description, job))
  }

  override suspend fun showHelp(kTerminal: KTerminal) {
    commands.mapValues { it.value.helpText() }.filter { it.value != null }.forEach {
      kTerminal.println(TextColors.green(TextStyles.bold(it.key) + ":\t${it.value}"))
    }
  }

  override suspend fun runCommand(
    kTerminal: KTerminal,
    cmdLine: String?,
    args: List<String>?
  ) {
    val args = args ?: parseCommandLineArgs(cmdLine!!)

    val cmdName = args.firstOrNull()?.trim() ?: "help"
    if (cmdName == "help") return showHelp(kTerminal)

    kTerminal.run {
      if (commands.contains(cmdName)) {
        commands[cmdName]?.invoke(this, args)
      } else {
        throw Errors.CommandNotFound(cmdName)
      }
    }
  }

  override suspend fun tabPressed(terminal: KTerminal) {

    val line = terminal.currentLine.toString()
    val cmdLine = line.trimStart()
    if (cmdLine.isBlank()) return

    val suggestions = commands.filterKeys { it.startsWith(cmdLine) }.keys
    if (suggestions.isEmpty()) return

    if (suggestions.size == 1) {
      val restOfCommand = suggestions.first().substringAfter(cmdLine)
      terminal.print(restOfCommand)
      terminal.currentLine.append(restOfCommand)
      terminal.cursorPos += restOfCommand.length
    } else {
      terminal.println()
      suggestions.forEach {
        terminal.print(it + '\t')
      }

      terminal.printPrompt(newLine = true)
      terminal.print(line)
      terminal.currentLine.append(line)
      terminal.cursorPos += line.length
    }
  }
}