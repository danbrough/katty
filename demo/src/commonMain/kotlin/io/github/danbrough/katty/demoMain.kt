package io.github.danbrough.katty

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Clock


class TestCommand() : CliktCommand("test") {

  val message by option().defaultLazy {  "Default message at ${Clock.System.now()}" }
  val count by option().int().default(1)

  override fun run() {
    print("test command. message: $message count: $count")
  }
}

fun demoMain(args: Array<String>) {

  /*
    listOf("/usr","/bin","/test.txt").map{Path(it)}.forEach { p->
      var md = SystemFileSystem.metadataOrNull(p)
      println("metadata for $p isDir: ${md?.isDirectory} isRegularFile: ${md?.isRegularFile}")
      val pp = SystemFileSystem.resolve(p)
      md = SystemFileSystem.metadataOrNull(pp)
      println("metadata for $pp isDir: ${md?.isDirectory} isRegularFile: ${md?.isRegularFile}")

    }
  */

  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")
  println("configDir: $configDir")
  if (!SystemFileSystem.exists(configDir)) {
    println("Creating configuration dir at $configDir..")
    SystemFileSystem.createDirectories(configDir, true)
  }
  val terminal = KTerminal(Path(configDir, "history.txt"))
  terminal.defaultCommandHandler = CliktDefaultCommandHandler().also {
    it.subcommands(TestCommand())
  }

  terminal.commandHandlers.addAll(listOf(LsCommandHandler, CommandRegex))
  terminal.run()
}