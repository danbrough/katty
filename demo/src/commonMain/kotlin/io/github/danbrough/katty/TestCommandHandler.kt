package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles

class TestCommandHandler(override val parent: CommandHandler) : CommandHandler {

  private val prompt = "test# ".let {
    it.length to (TextStyles.bold + TextColors.brightGreen)(it)
  }

  override suspend fun prompt(): Pair<Int, String> = prompt

  override suspend fun runCommand(
    kTerminal: KTerminal,
    cmdLine: String,
    args: List<String>?
  ) = kTerminal.run {
    println("TestCommandHandler: $cmdLine")
  }
}

val TestCommand = BasicCommand("initiates the TestCommandHandler") {
  push(TestCommandHandler(commandHandler))
}