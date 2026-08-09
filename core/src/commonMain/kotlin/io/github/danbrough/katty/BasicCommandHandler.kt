package io.github.danbrough.katty

open class BasicCommandHandler : CommandHandler {
  override fun runCommand(
    kTerminal: KTerminal,
    args: List<String>
  ) {
    kTerminal.println("BasicCommandHandler running with args: $args")
  }
}