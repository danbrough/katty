package io.github.danbrough.katty.command
/*

object CommandLine {

  */
/**
   * Parses a single Bash command string into a List of arguments,
   * respecting single quotes, double quotes, and backslash escape characters.
   *//*

  fun parseCommandLineArgs(arguments: String?): List<String> {
    if (arguments.isNullOrEmpty()) {
      return emptyList()
    }

    val argList = mutableListOf<String>()
    val currentArg = StringBuilder()

    var inDoubleQuotes = false
    var inSingleQuotes = false
    var isEscaped = false

    for (c in arguments) {
      when {
        isEscaped -> {
          currentArg.append(c)
          isEscaped = false
        }

        c == '\\' -> {
          // If we are inside single quotes, backslashes are literal
          if (inSingleQuotes) {
            currentArg.append(c)
          } else {
            isEscaped = true
          }
        }

        c == '"' -> {
          if (!inSingleQuotes) {
            inDoubleQuotes = !inDoubleQuotes
          } else {
            currentArg.append(c)
          }
        }

        c == '\'' -> {
          if (!inDoubleQuotes) {
            inSingleQuotes = !inSingleQuotes
          } else {
            currentArg.append(c)
          }
        }

        c.isWhitespace() -> {
          if (inDoubleQuotes || inSingleQuotes) {
            currentArg.append(c)
          } else if (currentArg.isNotEmpty()) {
            argList.add(currentArg.toString())
            currentArg.setLength(0)
          }
        }

        else -> {
          currentArg.append(c)
        }
      }
    }

    // Add the trailing argument if it exists
    if (currentArg.isNotEmpty()) {
      argList.add(currentArg.toString())
    }

    return argList
  }
}


*/
/*
fun main() {
  val bashCommand = "echo \"escaped 'single' quote\" --file=\\/path\\/to\\/file.txt 'hello world'"
  val parsed = BashCommandParser.parseArguments(bashCommand)

  println("Parsed Arguments:")
  parsed.forEachIndexed { index, arg ->
    println("[$index]: $arg")
  }
}
*/

