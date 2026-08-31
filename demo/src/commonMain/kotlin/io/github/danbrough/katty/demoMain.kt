package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.config.registerConfigCommands
import io.github.danbrough.katty.demos.DemoCoroutinesCommand
import io.github.danbrough.katty.demos.DemoMarkDownCommand
import io.github.danbrough.katty.demos.DemoMordantCommand
import io.github.danbrough.katty.demos.DemoThemeCommand
import kotlinx.coroutines.withContext
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

  val commandHandler: BasicCommandHandler = object : BasicCommandHandler() {
    val username = KattyUtils.getEnv("USER") ?: "user"

    override suspend fun prompt(): Pair<Int, String> {
      val parts = listOf("$username@katty ", Bashy.currentDir.toString(), " $ ")
      return parts.sumOf { it.length } to TextStyles.bold(
        TextColors.brightCyan(parts[0]) + TextColors.blue(
          parts[1] + parts[2]
        )
      )
    }
  }

  commandHandler.registerCommands(
    "markdownDemo" to DemoMarkDownCommand,
    "mordantDemo" to DemoMordantCommand,
    "themeDemo" to DemoThemeCommand,
    "coroutinesDemo" to DemoCoroutinesCommand,
    "test" to TestCommand,
  )

  commandHandler.registerBashyCommands()
  commandHandler.registerConfigCommands()

  val terminal =
    KTerminal(commandHandler, history = DefaultHistory(Path(configDir, "history.txt")))


  val app = KattyApplication<DemoAppConfig>()
  app.loadConfig(Path("demo/src/commonMain/resources/config.toml"))
  withContext(KattyApplicationElement(app)) {
    terminal.main(args)
  }
}