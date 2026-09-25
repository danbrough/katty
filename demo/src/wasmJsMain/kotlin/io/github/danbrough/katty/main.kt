package io.github.danbrough.katty

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


suspend fun main(args: Array<String>) {
  println("running node js demo .. args: ${args.joinToString()}")
  val job = coroutineScope {
    launch {
      //demoMain(args)
      SimpleTerminal(args.toList()).run()
    }
  }
  job.join()
}
