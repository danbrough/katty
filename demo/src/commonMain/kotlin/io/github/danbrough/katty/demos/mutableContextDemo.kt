package io.github.danbrough.katty.demos

import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.elementList
import io.github.danbrough.katty.shellContext


private var count = 0

private const val USAGE = "mutableDemo: (get | set | remove)"
fun mutableContextDemo() = basicCommand("mutableDemo", USAGE) {
  val args = it.toMutableList().drop(1)
  val action = args.getOrNull(0) ?: run {
    info(USAGE)
    return@basicCommand
  }

  when (action) {
    "get" -> {
      info(
        "thang: ${
          shellContext(ContextMessage) {
            ContextMessage("This is the message")
          }
        }"
      )
    }

    "set" -> {
      shellContext().addToContext(ContextMessage("message ${count++}"))
    }

    "remove" -> shellContext().removeFromContext(ContextMessage)

    "info" -> info("elements: [${shellContext().elementList().joinToString(" ")}]")
  }
}