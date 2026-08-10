package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem


open class Runner(val job: KTerminal.(List<String>) -> Unit) {

  operator fun invoke(kTerminal: KTerminal, args: List<String>) = job(kTerminal, args)
}

fun demoMain(args: Array<String>) {


  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    println((TextColors.brightMagenta + TextStyles.bold)("Creating configuration dir at $configDir..."))
    SystemFileSystem.createDirectories(configDir, true)
  }


  val commandHandler = DemoCommandHandler()

  val terminal =
    KTerminal(commandHandler, history = DefaultHistory(Path(configDir, "history.txt")))


  val runnerCommand = Runner { args ->
    args.forEach {
      println("runnerCommand: arg: $it")
    }
  }

  val testArgs = listOf("runnerCommandArg1", "arg2", "arg3")

  runnerCommand(terminal, testArgs)


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
    terminal.runCommand(cmdArgs, printNewLine = false)
    if (!interactive) return
  }
  terminal.run()
}