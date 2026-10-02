package io.github.danbrough.katty


import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import io.github.danbrough.katty.config.registerConfigCommands
import io.github.danbrough.katty.demos.ContextDemo
import io.github.danbrough.katty.demos.DemoMarkDownCommand
import io.github.danbrough.katty.demos.DemoMordantCommand
import io.github.danbrough.katty.demos.DemoThemeCommand
import io.github.danbrough.katty.demos.demoCoroutines
import io.github.danbrough.katty.demos.demoJobControl1
import io.github.danbrough.katty.demos.scopeDemo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.danbrough.klog.logger
import kotlin.time.Duration.Companion.seconds

internal val demoLog = logger("KATTY_DEMO")

suspend fun demoMain(args: Array<String>, vararg extraCommands: Pair<String, BasicCommand>) {
  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")

  if (!SystemFileSystem.exists(configDir)) {
    println((TextColors.brightMagenta + TextStyles.bold)("Creating configuration dir at $configDir..."))
    SystemFileSystem.createDirectories(configDir, true)
  }

  val app = DemoApp()
  //app.loadConfig(Path("./demo/src/commonMain/resources/config.toml")) TODO


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

  commandHandler.registerCommands(*extraCommands)
  commandHandler["coroutinesDemo", "Testing coroutines stuff"] = KTerminal::demoCoroutines

  commandHandler.registerCommands(
    "markdownDemo" to DemoMarkDownCommand,
    "mordantDemo" to DemoMordantCommand,
    "themeDemo" to DemoThemeCommand,
    scopeDemo, ContextDemo,
    basicCommand("forever", "Command that runs forever") {
      var n = 0
      while(true) {
        println("${n++}: Forever on thread ${KattyUtils.threadName()}")
        delay(1.seconds)
      }
    },
    demoJobControl1,
    basicCommand("testApp", "Check we can access the DemoApp and the KTerminal from the context") {
      println("the app is ${kattyApp<DemoApp>()}")
      println("terminal is ${currentCoroutineContext()[KTerminal]}")
      KattyUtils.atExit {
        println("DOING THIS AT EXIT")
      }
    },
  )

  commandHandler.registerBashyCommands()
  commandHandler.registerConfigCommands()

  val terminal =
    KTerminal(
      commandHandler,
      terminal = Terminal(theme = KattyTheme),
      cmdContext = app + KattyUtils.ioDispatcher,
      history = DefaultHistory(Path(configDir, "history.txt"))
    )

  runCatching {
    terminal.main(args.toList())
  }.exceptionOrNull()?.also {
    if (it !is CancellationException)
      println(it.stackTraceToString())
  }

}