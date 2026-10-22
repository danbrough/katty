package io.github.danbrough.katty.demos

import io.github.danbrough.katty.MutableContext
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.elementList
import kotlinx.coroutines.CoroutineName


val mutableContext = MutableContext(CoroutineName("Stuff"))


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
      info("thang: ${mutableContext[ContextMessage]}")
    }

    "set" -> {
      mutableContext.addToContext(ContextMessage("message ${count++}"))
    }

    "remove" -> mutableContext.removeFromContext(ContextMessage)

    "info" -> info("elements: [${mutableContext.elementList().joinToString(" ")}]")
  }
}