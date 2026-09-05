package io.github.danbrough.katty

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.tree.nodes.splitKeyValue
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles

class TestCommandHandler(override val parent: CommandHandler) : CommandHandler {

  private val prompt = "test# ".let {
    it.length to (TextStyles.bold + TextColors.brightGreen)(it)
  }

  override suspend fun prompt(): Pair<Int, String> = prompt

  val config = TomlInputConfig(allowEmptyValues = true)

  override suspend fun runCommand(
    kTerminal: KTerminal,
    cmdLine: String?,
    args: List<String>?
  ) {
    kTerminal.run {
      println("TestCommandHandler: $cmdLine")
      val parser = TomlParser(config)
      parseCommandLineArgs(cmdLine!!).forEach {
        println("ARG: [$it]")
        val parts = it.splitKeyValue(0,config)
        println("PARTS: $parts")
      }

    }
  }
}

val TestCommand =
  BasicCommand("initiates the TestCommandHandler that parses the command line as toml") {
    push(TestCommandHandler(commandHandler))
  }