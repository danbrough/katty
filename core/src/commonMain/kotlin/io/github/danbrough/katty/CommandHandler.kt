package io.github.danbrough.katty

fun interface CommandHandler {

  fun runCommand(kTerminal: KTerminal, args: List<String>)
}

open class TerminalCommand(private val job: KTerminal.(List<String>) -> Unit) {
  open operator fun invoke(kTerminal: KTerminal, args: List<String>) = job(kTerminal, args)
}
