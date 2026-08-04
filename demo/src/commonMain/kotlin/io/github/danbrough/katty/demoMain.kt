package io.github.danbrough.katty


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


fun demoMain(args: Array<String>) {


  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    //println("Creating configuration dir at $configDir...")
    SystemFileSystem.createDirectories(configDir, true)
  }
  val terminal = KTerminal(Path(configDir, "history.txt"))

  /*
    val cliktCommand = CliktDefaultCommandHandler(terminal).also {
      it.subcommands(TestCommand(), DateCommand())
    }
  */

  terminal.commandHandlers.addAll(listOf(LsCommandHandler, DateCommandHandler, TomlTestCommand))

  if (args.isNotEmpty()) terminal.runCommand(args.toList(), printNewLine = false)
  else terminal.run()
}