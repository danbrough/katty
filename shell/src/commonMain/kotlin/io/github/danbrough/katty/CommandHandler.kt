package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextStyles

interface CommandHandler {

  val parent: CommandHandler?

  suspend fun runCommand(kTerminal: KTerminal, cmdLine: String) =
    runCommand(kTerminal, parseCommandLine(cmdLine))

  suspend fun runCommand(kTerminal: KTerminal, args: List<String>)

  suspend fun showHelp(kTerminal: KTerminal) = Unit
  suspend fun tabPressed(terminal: KTerminal) = Unit

  fun parseCommandLine(cmdLine: String): List<String> = parseCommandLineArgs(cmdLine)

  /**
   * Return the string length of the prompt and the formatted prompt itself
   */
  suspend fun prompt(): Pair<Int, String> = 2 to TextStyles.bold("# ")

}


/**
 * Parse the [input] into a list of command-line arguments
 */
private fun parseCommandLineArgs(input: String): List<String> {
  val args = mutableListOf<String>()
  val current = StringBuilder()
  var inQuotes = false
  var quoteChar: Char = '"'
  var escapeNext = false

  for (char in input) {
    when {
      escapeNext -> {
        current.append(char)
        escapeNext = false
      }

      char == '\\' && inQuotes -> {
        escapeNext = true
      }

      char in setOf('"', '\'') && !inQuotes -> {
        inQuotes = true
        quoteChar = char
      }

      char == quoteChar && inQuotes -> {
        inQuotes = false
      }

      char.isWhitespace() && !inQuotes -> {
        if (current.isNotEmpty()) {
          args.add(current.toString())
          current.clear()
        }
      }

      else -> {
        current.append(char)
      }
    }
  }

  if (current.isNotEmpty()) {
    args.add(current.toString())
  }

  return args
}