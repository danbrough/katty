package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.config.ConfigDemoCommand
import io.github.danbrough.katty.demos.DemoCoroutinesCommand
import io.github.danbrough.katty.demos.DemoMarkDownCommand
import io.github.danbrough.katty.demos.DemoMordantCommand
import io.github.danbrough.katty.demos.DemoThemeCommand
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.danbrough.klog.logger

val log = logger("KATTY_DEMO")

suspend fun demoMain(args: Array<String>) {


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
    "exit" to Bashy.ExitCommand,
    "configDemo" to ConfigDemoCommand,
    "markdownDemo" to DemoMarkDownCommand,
    "mordantDemo" to DemoMordantCommand,
    "themeDemo" to DemoThemeCommand,
    "coroutinesDemo" to DemoCoroutinesCommand,
  )

  val terminal =
    KTerminal(commandHandler, history = DefaultHistory(Path(configDir, "history.txt")))


  val username = KattyUtils.getEnv("USER") ?: "user"

  terminal.prompt = {
    val parts = listOf("$username@katty ", Bashy.currentDir.toString(), " $ ")
    parts.sumOf { it.length } to TextStyles.bold(TextColors.brightCyan(parts[0]) + TextColors.blue(parts[1] + parts[2]))
  }

  terminal.main(args)
}