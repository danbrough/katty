package io.github.danbrough.katty

/**
 * Parse the [input] into a list of command-line arguments
 */
fun parseCommandLineArgs(input: String): List<String> {
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