package io.github.danbrough.katty

import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
  runBlocking {
    if (args.getOrNull(0) == "simple") {
      SimpleTerminal(args.toList()).run()
    } else {
      demoMain(args)
    }
  }
}