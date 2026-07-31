package io.github.danbrough.katty

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Clock


class TestCommand() : CliktCommand("test") {

  val message by option().defaultLazy { "Default message at ${Clock.System.now()}" }
  val count by option().int().default(1).help("How many times to print the message")

  override fun run() {
    for (n in 1..count)
      kTerminal.terminal.println(TextStyles.bold(TextColors.brightMagenta("test command. message: $message count: $n")))
  }
}


class DateCommand() : CliktCommand("date") {
  override fun run() {
    val tz = TimeZone.currentSystemDefault()
    val date = Clock.System.now().toLocalDateTime(tz)
    kTerminal.terminal.print(TextStyles.bold(TextColors.brightCyan(("$commandName: $date $tz"))))
  }
}


fun demoMain(args: Array<String>) {


  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    //println("Creating configuration dir at $configDir..")
    SystemFileSystem.createDirectories(configDir, true)
  }
  val terminal = KTerminal(Path(configDir, "history.txt"))
  terminal.defaultCommandHandler = CliktDefaultCommandHandler(terminal).also {
    it.subcommands(TestCommand(), DateCommand())
  }

  terminal.commandHandlers.addAll(listOf(LsCommandHandler, CommandRegex))

  if (args.isNotEmpty())
    terminal.runCommand(args.toList(), printNewLine = false)
  else
    terminal.run()
}