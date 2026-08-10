package io.github.danbrough.katty


import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.CliktCommandHandler.Companion.CTX_KEY_KTERMINAL
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Clock


fun demoMain(args: Array<String>) {


  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  val bashContext = BashContext()

  if (!SystemFileSystem.exists(configDir)) {
    println((TextColors.brightMagenta + TextStyles.bold)("Creating configuration dir at $configDir..."))
    SystemFileSystem.createDirectories(configDir, true)
  }

  class DateCommand : CliktCommand("date") {
    val kTerminal: KTerminal by requireObject<KTerminal>(CTX_KEY_KTERMINAL)

    val message by option().defaultLazy {
      "The date generated at ${Clock.System.now()} is:"
    }

    override fun help(context: Context): String = "Prints the date"

    override fun run() {
      val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
      kTerminal.println(TextColors.brightMagenta("$message $date"))
    }
  }

  val commandHandler = CliktCommandHandler()

  commandHandler.commands["date"] = ::DateCommand
  commandHandler.commands["pwd"] = {
    object : CliktCommand("pwd") {
      val message by requireObject<String>("message")
      val kTerminal: KTerminal by requireObject<KTerminal>(CTX_KEY_KTERMINAL)


      override fun help(context: Context): String = "Prints something"
      override fun run() {
        kTerminal.println("running pwd .. rootCommand message is $message")
      }
    }
  }


  val terminal =
    KTerminal(commandHandler, history = DefaultHistory(Path(configDir, "history.txt")))


  val username = KattyUtils.getEnv("USER") ?: "user"

  terminal.prompt = {
    val part = listOf("$username@katty ", bashContext.currentDir.toString(), " $ ")
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
    terminal.runCommand(cmdArgs, printNewLine = false)
    if (!interactive) return
  }
  terminal.run()
}