package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.config.ConfigDemoCommand
import io.github.danbrough.katty.demos.DemoMarkDownCommand
import io.github.danbrough.katty.demos.DemoMordantCommand
import io.github.danbrough.katty.demos.DemoThemeCommand
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem


fun demoMain(args: Array<String>) {


  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    println((TextColors.brightMagenta + TextStyles.bold)("Creating configuration dir at $configDir..."))
    SystemFileSystem.createDirectories(configDir, true)
  }


  val commandHandler = BasicCommandHandler()

  commandHandler.registerCommands(
    "pwd" to Bashy.PwdCommand,
    "date" to Bashy.DateCommand,
    "ls" to Bashy.LsCommand,
    "cd" to Bashy.CdCommand,
    "demoConfig" to ConfigDemoCommand,
    "demoMarkdown" to DemoMarkDownCommand,
    "demoMordant" to DemoMordantCommand,
    "demoTheme" to DemoThemeCommand,
  )

  val terminal =
    KTerminal(commandHandler, history = DefaultHistory(Path(configDir, "history.txt")))


  val username = KattyUtils.getEnv("USER") ?: "user"

  terminal.prompt = {
    val part = listOf("$username@katty ", Bashy.currentDir.toString(), " $ ")
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