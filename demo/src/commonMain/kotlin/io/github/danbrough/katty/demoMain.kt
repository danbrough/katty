package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.table.grid
import com.github.ajalt.mordant.terminal.Terminal
import io.github.danbrough.katty.Bash.DateCommandHandler
import io.github.danbrough.katty.Bash.PwdCommand
import io.github.danbrough.katty.config.ConfigTestCommand
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem


/*
class TestCommand() : CliktCommand("test") {

  val message by option().defaultLazy { "Default message at ${Clock.System.now()}" }
    .help("Message to print")
  val count by option().int().default(1).help("How many times to print the message")

  override fun run() {
    for (n in 1..count) kTerminal.terminal.print(TextStyles.bold(TextColors.brightMagenta("test command. message: $message count: $n${if (n < count) SystemLineSeparator else ""}")))
  }
}


class DateCommand() : CliktCommand("date") {
  override fun run() {
    val tz = TimeZone.currentSystemDefault()
    val date = Clock.System.now().toLocalDateTime(tz)
    kTerminal.terminal.print(TextStyles.bold(TextColors.brightCyan(("$commandName: $date $tz"))))
  }
}
*/


fun markdownTest() {
  val t = Terminal()
  t.println("Markdown test")
  t.render(grid {
    row("Grid Builder", "Supports", "Alignment")
    row {
      cell("Left") { align = TextAlign.LEFT }
      cell("Center") { align = TextAlign.CENTER }
      cell("Right") { align = TextAlign.RIGHT }
    }
  })

  t.println("FINISHED")
}

fun demoMain(args: Array<String>) {

  when (args.firstOrNull()) {
    "markdown" -> markdownTest()
    //"mordant" -> demoMordant()
    else -> {}
  }

  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    //println("Creating configuration dir at $configDir...")
    SystemFileSystem.createDirectories(configDir, true)
  }

  /*val terminal = KTerminal(Terminal(theme = Theme{
    styles["hr.rule"] = rgb("#24218c")
    styles["panel.border"] = rgb("#24218c")
  }),Path(configDir, "history.txt"))*/


  val terminal = KTerminal(Terminal(),Path(configDir, "history.txt"))

  terminal.commandHandlers.addAll(
    listOf(
      LsCommandHandler,
      DateCommandHandler,
      PwdCommand,
      DemoMordantCommand,
      Bash.CdCommand,
      ConfigTestCommand,
      TomlTestCommand,
    )
  )

  if (args.isNotEmpty()) terminal.runCommand(args.toList(), printNewLine = false)
  else terminal.run()
}