package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem


fun demoMain(args: Array<String>) {


  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    println((TextColors.brightMagenta + TextStyles.bold)("Creating configuration dir at $configDir..."))
    SystemFileSystem.createDirectories(configDir, true)
  }


  val commandHandler = DemoCommandHandler()

  commandHandler.registerCommands(
    "pwd" to Bash.PwdCommand,
    "date" to Bash.DateCommand,
    "ls" to Bash.LsCommand,
    "cd" to Bash.CdCommand,
  )

  val terminal =
    KTerminal(commandHandler, history = DefaultHistory(Path(configDir, "history.txt")))


  val username = KattyUtils.getEnv("USER") ?: "user"

  terminal.prompt = {
    val part = listOf("$username@katty ", Bash.currentDir.toString(), " $ ")
    part.sumOf { it.length } to TextStyles.bold(
      TextColors.brightCyan(part[0]) + TextColors.blue(
        part[1] + part[2]
      )
    )
  }

  /*  terminal.commandHandlerOlds.addAll(
      listOf(
        LsCommandHandler,
        DateCommandHandler,
        PwdCommand,
        DemoMordantCommand,
        DemoMarkdownCommand,
        Bash.CdCommand,
        ConfigTestCommand,
        DemoTomlCommand,
        DemoThemeCommand,
        Ctx(),
      )
    )*/

  if (args.isNotEmpty()) {
    val interactive = args.firstOrNull() == "-i"
    val cmdArgs = if (interactive) args.drop(1) else args.toList()
    terminal.runCommand(args = cmdArgs, printNewLine = false)
    if (!interactive) return
  }
  terminal.run()
}