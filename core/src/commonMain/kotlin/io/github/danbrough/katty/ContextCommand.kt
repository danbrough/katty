package io.github.danbrough.katty

val ContextCommand =
  BasicCommand("context", "show variables from the context. usage: context [-d] <name>") {
    it.drop(1).forEach { name ->
      println(terminal.theme.info("$name: ${context[name].toString()}"))
    }
  }