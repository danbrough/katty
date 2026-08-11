package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.Bashy.Theme.normal
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import kotlin.time.Clock

/**
 * Some example commands
 */
object Bashy {


  /**
   * THe "current directory" for the [LsCommand],[CdCommand] and [PwdCommand]
   */
  var currentDir = SystemFileSystem.resolve(Path("."))

  object Theme {
    val normal = TextColors.green
  }

  /**
   * Prints the value of [currentDir]
   */
  val PwdCommand = TerminalCommand("prints the current directory") {
    println(normal(currentDir.toString()))
  }

  /**
   * Prints the date
   */

  val DateCommand = object : TerminalCommand("prints the date") {
    val format = LocalDateTime.Format {
      year()
      char('-')
      monthNumber()
      char('-')
      day(padding = Padding.ZERO)

      char(' ')
      dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
      char(' ')
      monthName(MonthNames.ENGLISH_ABBREVIATED)
      char(' ')
      day(padding = Padding.SPACE)
      char(' ')


      hour()
      char(':')
      minute()
      char(':')
      second()
    }

    override fun invoke(kTerminal: KTerminal, args: List<String>) {
      val tz = TimeZone.currentSystemDefault()
      val dateTime = Clock.System.now().toLocalDateTime(tz)
      println(normal(dateTime.format(format) + " ${tz.id}"))
    }
  }

  val LsCommand =
    TerminalCommand("usage: ls [dir]. prints the contents of the current or the specified directory") { args ->

      val dir = if (args.size == 1) currentDir else {
        val arg = args[1]
        if (arg == ".") currentDir
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
            if (it.first.toString() != resolvedPath.toString()) TextColors.brightGreen else if (it.second?.isDirectory == true) TextColors.green else
              TextColors.white
          style(it.first.name)
        }.also {
          println(it)
          cursorPos = 0
        }
    }


  val CdCommand = TerminalCommand("usage: cd [dir]. changes the current directory") { args ->

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