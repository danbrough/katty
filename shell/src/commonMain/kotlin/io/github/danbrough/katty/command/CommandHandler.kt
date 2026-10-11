package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextStyles

private val DEFAULT_PROMPT = TextStyles.bold("# ")
private const val DEFAULT_PROMPT_STRLEN = 2

interface CommandHandler {

  val parent: CommandHandler?

  suspend fun runCommand(shell: KattyShell, cmdLine: String) =
    runCommand(shell, parseCommandLine(cmdLine))

  suspend fun runCommand(shell: KattyShell, args: List<String>)

  suspend fun showHelp(shell: KattyShell) = Unit
  suspend fun tabPressed(terminal: KattyShell) = Unit

  fun parseCommandLine(cmdLine: String): List<String> = ArgumentTokenizer.tokenize(cmdLine)

  /**
   * Return the string length of the prompt and the formatted prompt itself
   */
  fun prompt(): Pair<Int, String> = DEFAULT_PROMPT_STRLEN to DEFAULT_PROMPT
}

