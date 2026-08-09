package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import kotlin.time.Clock



/**
 * Some shell commands for the demo
 */

object LsCommandHandler : CommandHandlerOld {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "ls"


  override fun exec(kTerminal: KTerminal, args: List<String>) {
    val dir = if (args.size == 1) Bash.currentDir else {
      if (args[1] == ".") Bash.currentDir
      else if (args[1] == "..") Path(Bash.currentDir, "..")
      else if (args[1].startsWith(SystemPathSeparator)) Path(args[1])
      else Path(Bash.currentDir, args[1])
    }

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

object Bash {

  var currentDir: Path = SystemFileSystem.resolve(Path("."))

  val DateCommandHandler = BasicCommand("date", "prints the date") {
    val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    println("The date is $date")
  }

  val PwdCommand = BasicCommand("pwd", "prints the current working directory") {
    println(currentDir)
  }

  object CdCommand : BasicCommand("cd", "changes the current directory") {

    val home = KattyUtils.getEnv("HOME")?.let { SystemFileSystem.resolve(Path(it)) } ?: currentDir

    override fun exec(kTerminal: KTerminal, args: List<String>) {
      if (args.size == 1) {
        currentDir = home
      } else {
        currentDir = when (args[1]) {
          "." -> currentDir
          ".." -> Path(currentDir, "..")
          else -> if (args[1].startsWith(SystemPathSeparator)) Path(args[1]) else Path(
            currentDir,
            args[1]
          )
        }
        currentDir = SystemFileSystem.resolve(currentDir)
      }
      println("changed to $currentDir")
    }
  }
}


