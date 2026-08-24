package io.github.danbrough.katty

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles

fun interface CommandHandler {
  suspend fun runCommand(kTerminal: KTerminal, args: List<String>)

  suspend fun showHelp(kTerminal: KTerminal) = Unit
  suspend fun tabPressed(terminal: KTerminal) = Unit

  /**
   * Return the string length of the prompt and the formatted prompt itself
   */
  suspend fun prompt(): Pair<Int,String> = 2 to TextStyles.bold("# ")

}

