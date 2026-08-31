package io.github.danbrough.katty

class TestCommandHandler(override val parent: CommandHandler) : CommandHandler {
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