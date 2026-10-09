package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

private val log = logKattyShell


open class BasicCommandHandler(override val parent: CommandHandler? = null) : CommandHandler {

  override suspend fun prompt(): Pair<Int, String> = "$ ".let {
    it.length to TextStyles.bold(TextColors.brightGreen(it))
  }

  private val commands = mutableMapOf<String, BasicCommand>()

  fun registerCommands(vararg cmds: Pair<String, BasicCommand>) {
    commands.putAll(cmds)
  }

  operator fun set(name: String, description: String, job: BasicCommandJob) {
    registerCommands(name to BasicCommand(description, job))
  }

  override suspend fun showHelp(kTerminal: KTerminal) {
    commands.mapValues { it.value.helpText() }.filter { it.value != null }.forEach {
      kTerminal.println(TextColors.green(TextStyles.bold(it.key) + ":\t${it.value}"))
    }
  }

  override suspend fun runCommand(
    kTerminal: KTerminal,
    args: List<String>
  ) {
    val cmdName = args.firstOrNull()?.trim() ?: return showHelp(kTerminal)

    kTerminal.run {
      if (commands.contains(cmdName)) {
        commands[cmdName]?.invoke(this, args)
      } else {
        throw Errors.CommandNotFound(cmdName)
      }
    }
  }

  var logWriter = SystemFileSystem.sink(Path("/tmp/test.log")).buffered()

  override suspend fun tabPressed(terminal: KTerminal) {

    logWriter.writeString("tabPressed linePos: ${terminal.linePos}\n")

    fun lastCommonPrefixPosition(strings: Set<String>): Int {
      if (strings.isEmpty()) return -1
      if (strings.size == 1) return strings.first().length

      val shortest = strings.minByOrNull { it.length } ?: return 0


      return shortest.foldIndexed(0) { index, acc, char ->
        if (strings.all { it[index] == char }) index + 1 else acc
      }
    }

    val line = terminal.currentLine.toString()
    val cmdLine = line.trimStart()
    if (cmdLine.isBlank()) return

    val suggestions = commands.filterKeys { it.startsWith(cmdLine) }.keys
    if (suggestions.isEmpty()) return
    logWriter.writeString("suggestions: [${suggestions.joinToString(",")}]\n")
    val commonPrefixPosition = lastCommonPrefixPosition(suggestions)
    logWriter.writeString("commonPrefix: $commonPrefixPosition linePos:${terminal.linePos} line [$line] length: ${line.length}\n")
    //println("COMMON PREFIX: $commonPrefixPosition linePos:${cmdLine.length}")
    if (cmdLine.length < commonPrefixPosition) {
      val rest =
        suggestions.first().substring(cmdLine.length).take(commonPrefixPosition - cmdLine.length)
      logWriter.writeString("rest: [$rest]\n")
      terminal.print(rest)
      terminal.currentLine.append(rest)
      //terminal.cursorPos += rest.length
    } else {
      terminal.println()
      suggestions.forEach {
        terminal.print(it + '\t')
      }

      terminal.printPrompt(newLine = true)
      terminal.print(line)
      terminal.currentLine.append(line)
      terminal.cursorPos = line.length + terminal.promptLength
    }

    logWriter.flush()
  }
}