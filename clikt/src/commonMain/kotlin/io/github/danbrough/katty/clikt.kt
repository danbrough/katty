package io.github.danbrough.katty

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.parse


open class CliktDefaultCommandHandler(val kTerminal: KTerminal) : CommandHandler, CliktCommand("katty") {

  override val allowMultipleSubcommands: Boolean = true

  init {
    context {
      helpOptionNames = helpOptionNames.toMutableList().also {
        it.addAll(listOf("help"))
      }
    }
  }


  override fun matches(args: List<String>): Boolean {
    TODO("Not yet implemented")
  }

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(kTerminal: KTerminal, args: List<String>) {
    runCatching {
      parse(args)
    }.exceptionOrNull()?.also {
      if (it is CliktError) {
        echoFormattedHelp(it)
      } else throw it
    }
  }

  override fun run() {
    currentContext.data["kTerminal"] = kTerminal
  }
}

val Context.kTerminal: KTerminal
  get() = data["kTerminal"] as KTerminal

val CliktCommand.kTerminal: KTerminal
  get() = currentContext.kTerminal




