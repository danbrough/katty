package io.github.danbrough.katty

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.parse


open class CliktDefaultCommandHandler() : CommandHandler, CliktCommand("katty") {

  //override val allowMultipleSubcommands: Boolean = true

  fun splitCommandLine(command: String): List<String> {
    val list = mutableListOf<String>()
    // Matches double-quoted strings, single-quoted strings, or unquoted words
    val regex = Regex("\"([^\"]*)\"|'([^']*)'|(\\S+)")
    val matches = regex.findAll(command)

    for (match in matches) {
      when {
        match.groups[1] != null -> list.add(match.groups[1]!!.value) // Double quotes
        match.groups[2] != null -> list.add(match.groups[2]!!.value) // Single quotes
        match.groups[3] != null -> list.add(match.groups[3]!!.value) // Unquoted word
      }
    }
    return list
  }

  override fun matches(cmdLine: String): Boolean {
    TODO("Not yet implemented")
  }

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(kTerminal: KTerminal, cmdLine: String) {
    runCatching {
      val args = splitCommandLine(cmdLine)
      println("args: $args")
      parse(args.toTypedArray())
    }.exceptionOrNull()?.also {
      if (it is CliktError) {
        echoFormattedHelp(it)
      } else throw it
    }
  }

  override fun run() {}

}




