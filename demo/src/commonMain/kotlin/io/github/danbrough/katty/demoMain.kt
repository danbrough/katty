package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import io.github.danbrough.katty.Bash.DateCommandHandler
import io.github.danbrough.katty.Bash.PwdCommand
import io.github.danbrough.katty.cmds.Ctx
import io.github.danbrough.katty.config.ConfigTestCommand
import io.github.danbrough.katty.config.DemoTomlCommand
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem


fun demoMain(args: Array<String>) {


  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    //println("Creating configuration dir at $configDir...")
    SystemFileSystem.createDirectories(configDir, true)
  }

  /*val terminal = KTerminal(Terminal(theme = Theme{
    styles["hr.rule"] = rgb("#24218c")
    styles["panel.border"] = rgb("#24218c")
  }),Path(configDir, "history.txt"))*/


  val terminal = KTerminal(Terminal(), Path(configDir, "history.txt"))
  terminal.prompt = {
    val part = listOf("katty ", Bash.currentDir.toString(), " $ ")
    promptLength = part.sumOf { it.length }
    TextStyles.bold(TextColors.brightGreen(part[0]) + TextColors.blue(part[1] + part[2]))
  }

  terminal.commandHandlers.addAll(
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
  )

  if (args.isNotEmpty()) {
    val interactive = args.firstOrNull() == "-i"
    val cmdArgs = if (interactive) args.drop(1) else args.toList()
    terminal.runCommand(cmdArgs, printNewLine = false)
    if (!interactive) return
  }
  terminal.run()
}