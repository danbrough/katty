package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.Bash.Theme.normal
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import kotlin.time.Clock

object Bash {
  var currentDir = SystemFileSystem.resolve(Path("."))

  object Theme {
    val normal = TextColors.green
  }

  val PwdCommand = TerminalCommand {
    println(normal(currentDir.toString()))
  }

  val DateCommand = TerminalCommand {
    println(normal(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()))
  }

  val LsCommand = TerminalCommand { args ->
    println("ARGS: $args")
    val dir = if (args.size == 1) currentDir else {
      val arg = args[1]
      if (arg == ".") Bash.currentDir
      else if (arg == "..") Path(currentDir, "..")
      else if (arg.startsWith(SystemPathSeparator)) Path(arg)
      else Path(currentDir, arg)
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
        println(it)
        cursorPos = 0
      }
  }


  val CdCommand = TerminalCommand { args ->

    val home = KattyUtils.getEnv("HOME")?.let { SystemFileSystem.resolve(Path(it)) } ?: currentDir

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