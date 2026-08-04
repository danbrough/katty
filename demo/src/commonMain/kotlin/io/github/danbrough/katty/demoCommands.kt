package io.github.danbrough.katty

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.writers.TomlWriter
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.SystemLineSeparator
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlin.time.Clock

/**
 * Some shell commands for the demo
 */

object LsCommandHandler : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "ls"

  override fun addCompletion(completions: MutableList<String>) {

  }

  override fun exec(kTerminal: KTerminal, args: List<String>) {
    val dir = Path(args.getOrNull(1) ?: ".")
    //println("dir $dir")

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
        kTerminal.terminal.println(it)
        kTerminal.cursorPos = 0
      }
  }

  override fun helpText(): String = "ls [directory name]: Lists the contents of a directory"

}


object DateCommandHandler : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "date"

  override fun exec(kTerminal: KTerminal, args: List<String>) {

    val msg = "The date is ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}"
    kTerminal.terminal.rawPrint("${TextStyles.bold(msg)}$SystemLineSeparator")
    kTerminal.cursorPos = 0
  }

  override fun helpText(): String = "date: Prints the current date"

}

object TomlTestCommand : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "tomlTest"
  override fun helpText(): String = "tomlTest: usage: tomlTest [path to file]"

  override fun exec(
    kTerminal: KTerminal,
    args: List<String>
  ) {

    val path = SystemFileSystem.resolve(Path(args[1]))

    kTerminal.terminal.println("path: $path")
    val lines = sequence {
      SystemFileSystem.source(path).buffered().use { source ->
        while (true) {
          val line = source.readLine() ?: return@sequence
          kTerminal.terminal.println("LINE [$line]")
          yield(line)
        }
      }
    }
    kTerminal.terminal.println("LINES: ${lines.toList()}")

    val file = TomlParser(TomlInputConfig()).parseLines(lines)
    kTerminal.terminal.println("file: ${file.prettyStr()}")
    kTerminal.cursorPos = 0
    TomlWriter(TomlOutputConfig()).writeToString(file).also {
      kTerminal.terminal.println("file2: $it")
    }

  }

}