package io.github.danbrough.katty


import com.github.ajalt.mordant.terminal.Terminal
import io.github.danbrough.katty.Bash.DateCommandHandler
import io.github.danbrough.katty.Bash.PwdCommand
import io.github.danbrough.katty.config.ConfigTestCommand
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

  terminal.commandHandlers.addAll(
    listOf(
      LsCommandHandler,
      DateCommandHandler,
      PwdCommand,
      DemoMordantCommand,
      DemoMarkdownCommand,
      Bash.CdCommand,
      ConfigTestCommand,
      TomlTestCommand,
    )
  )

  if (args.isNotEmpty()) terminal.runCommand(args.toList(), printNewLine = false)
  else terminal.run()
}