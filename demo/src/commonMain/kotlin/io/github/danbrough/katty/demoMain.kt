package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.config.registerConfigCommands
import io.github.danbrough.katty.demos.DemoMarkDownCommand
import io.github.danbrough.katty.demos.DemoMordantCommand
import io.github.danbrough.katty.demos.DemoThemeCommand
import io.github.danbrough.katty.demos.demoCoroutines
import io.github.danbrough.katty.demos.demoJobControl1
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.danbrough.klog.logger

internal val demoLog = logger("KATTY_DEMO")

suspend fun demoMain(args: Array<String>) {
  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    println((TextColors.brightMagenta + TextStyles.bold)("Creating configuration dir at $configDir..."))
    SystemFileSystem.createDirectories(configDir, true)
  }

  val app = DemoApp()
  app.loadConfig(Path("./demo/src/commonMain/resources/config.toml"))


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


  commandHandler["coroutinesDemo", "Testing coroutines stuff"] = KTerminal::demoCoroutines

  commandHandler.registerCommands(
    "markdownDemo" to DemoMarkDownCommand,
    "mordantDemo" to DemoMordantCommand,
    "themeDemo" to DemoThemeCommand,
    "test" to TestCommand,
    demoJobControl1,
    basicCommand("testApp", "Check we can access the DemoApp") {
      println("the app is ${kattyApp<DemoApp>()}")
      println("terminal is ${currentCoroutineContext()[KTerminal]}")
    },
  )

  commandHandler.registerBashyCommands()
  commandHandler.registerConfigCommands()


  val terminal =
    KTerminal(
      commandHandler,
      context = KattyUtils.ioDispatcher + app,
      history = DefaultHistory(Path(configDir, "history.txt"))
    )

  runCatching {
    terminal.main(args)
  }.exceptionOrNull()?.also {
    if (it !is CancellationException)
      println(it.stackTraceToString())
  }

}