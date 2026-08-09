package io.github.danbrough.katty.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.github.danbrough.katty.CommandHandlerOld
import io.github.danbrough.katty.KTerminal
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

abstract class CliktCommandHandler(name: String) : CliktCommand(name), CommandHandlerOld {

  override fun matches(args: List<String>): Boolean = args.firstOrNull() == commandName

  override fun helpText(): String = help(currentContext)

  override fun exec(
    kTerminal: KTerminal,
    args: List<String>
  ) {
    println("exec called with args: $args")
    runCatching {
      parse(args.drop(1))
    }.exceptionOrNull()?.also {
      if (it is CliktError) {
        echoFormattedHelp(it)
      } else throw it
    }
  }
}

val Context.kTerminal: KTerminal
  get() = data["kTerminal"] as KTerminal

val CliktCommand.kTerminal: KTerminal
  get() = currentContext.kTerminal


class Ctx(name: String = "ctx") : CliktCommandHandler(name) {
  val all by option().flag()
  val message: String by option().defaultLazy {
    "The message for $commandName was created at ${
      Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }"
  }

  override fun run() {
    println("$commandName: all: $all  message:$message")
  }
}