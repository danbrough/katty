package io.github.danbrough.katty

class CliktCommandHandler : CommandHandler {
  override fun completions(cmdLine: String): List<String> = emptyList()

  override fun runCommand(
    kTerminal: KTerminal,
    args: List<String>
  ) {
    kTerminal.println("runCommand called with args: $args")
  }
}