package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Clock

object LsCommandHandler : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "ls"

  override fun addCompletion(completions: MutableList<String>) {

  }

  override fun exec(kTerminal: KTerminal, args: List<String>) {
    val dir = Path(args.getOrNull(1) ?: ".")
    //println("dir $dir")

    kTerminal.terminal.println()

    SystemFileSystem.list(SystemFileSystem.resolve(Path(dir)))
      .map { it to SystemFileSystem.metadataOrNull(it) }
      .joinToString("\n") {
        val resolvedPath = SystemFileSystem.resolve(it.first)
        //println("resolved path: $resolvedPath path: ${it.first} equal: ${resolvedPath == it.first}")
        val style =
          if (it.first.toString() != resolvedPath.toString()) TextColors.brightCyan else if (it.second?.isDirectory == true) TextColors.blue else
            TextColors.white
        TextStyles.bold(style(it.first.name))
      }.also {
        kTerminal.terminal.rawPrint(it)
      }

  }

  override fun helpText(): String = "ls [directory name]: Lists the contents of a directory"

}


object DateCommandHandler : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "date"

  override fun addCompletion(completions: MutableList<String>) {

  }

  override fun exec(kTerminal: KTerminal, args: List<String>) {
    val msg = "The date is ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}"
    kTerminal.terminal.print(TextStyles.bold(msg))
  }

  override fun helpText(): String = "date: Prints the current date"

}