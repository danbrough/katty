package io.github.danbrough.katty.command

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.CommandHandler
import io.github.danbrough.katty.Errors
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.logKattyShell
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

private val log = logKattyShell


open class BasicCommandHandler(override val parent: CommandHandler? = null) : CommandHandler {

  override fun prompt(): Pair<Int, String> = "$ ".let {
    it.length to TextStyles.bold(TextColors.brightGreen(it))
  }

  private val commands = mutableMapOf<String, BasicCommand>()

  fun registerCommands(vararg cmds: Pair<String, BasicCommand>) {
    commands.putAll(cmds)
  }

  operator fun set(name: String, description: String, job: BasicCommandJob) {
    registerCommands(name to BasicCommand(description, job))
  }

  override suspend fun showHelp(shell: KattyShell) {
    commands.mapValues { it.value.helpText() }.filter { it.value != null }.forEach {
      shell.info(TextColors.green(TextStyles.bold(it.key) + ":\t${it.value}"))
    }
  }

  override suspend fun runCommand(
    shell: KattyShell,
    args: List<String>
  ) {
    val cmdName = args.firstOrNull()?.trim() ?: return showHelp(shell)

    shell.run {
      if (commands.contains(cmdName)) {
        commands[cmdName]?.invoke(this, args)
      } else {
        throw Errors.CommandNotFound(cmdName)
      }
    }
  }

  var logWriter = SystemFileSystem.sink(Path("/tmp/test.log")).buffered()

  override suspend fun tabPressed(shell: KattyShell) {
    logWriter.writeString("tabPressed linePos: ${shell.linePos}\n")

    fun lastCommonPrefixPosition(strings: Set<String>): Int {
      if (strings.isEmpty()) return -1
      if (strings.size == 1) return strings.first().length

      val shortest = strings.minByOrNull { it.length } ?: return 0


      return shortest.foldIndexed(0) { index, acc, char ->
        if (strings.all { it[index] == char }) index + 1 else acc
      }
    }

    val line = shell.currentLine.toString()
    val cmdLine = line.trimStart()
    if (cmdLine.isBlank()) return

    val suggestions = commands.filterKeys { it.startsWith(cmdLine) }.keys
    if (suggestions.isEmpty()) return
    logWriter.writeString("suggestions: [${suggestions.joinToString(",")}]\n")
    val commonPrefixPosition = lastCommonPrefixPosition(suggestions)
    logWriter.writeString("commonPrefix: $commonPrefixPosition linePos:${shell.linePos} line [$line] length: ${line.length}\n")
    //println("COMMON PREFIX: $commonPrefixPosition linePos:${cmdLine.length}")
    if (cmdLine.length < commonPrefixPosition) {
      val rest =
        suggestions.first().substring(cmdLine.length).take(commonPrefixPosition - cmdLine.length)
      logWriter.writeString("rest: [$rest]\n")
      shell.print(rest)
      shell.currentLine.append(rest)
      //terminal.cursorPos += rest.length
    } else {
      shell.println()
      suggestions.forEach {
        shell.print(it + '\t')
      }

      shell.printPrompt(newLine = true)
      shell.print(line)
      shell.currentLine.append(line)
      shell.cursorPos = line.length + shell.promptLength
    }

    logWriter.flush()
  }
}