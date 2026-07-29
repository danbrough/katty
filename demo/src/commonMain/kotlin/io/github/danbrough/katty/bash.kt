package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.io.SystemLineSeparator
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

object LsCommandHandler : CommandHandler {
  override fun matches(cmdLine: String): Boolean =
    cmdLine.matches("^\\s?ls\\s?.*".toRegex())

  override fun addCompletion(completions: MutableList<String>) {
    TODO("Not yet implemented")
  }

  override fun exec(kTerminal: KTerminal, cmdLine: String) {
    val dir = if (cmdLine != "ls") cmdLine.substringAfter("ls ") else "."
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

}