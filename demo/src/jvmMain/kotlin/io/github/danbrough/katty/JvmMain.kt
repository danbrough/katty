package io.github.danbrough.katty

import kotlinx.coroutines.runBlocking

class JvmMain {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      demoLog.info { "${KattyUtils.threadName()}: in JvmMain.main()" }
      runBlocking {
        if (args.getOrNull(0) == "simple") {
          SimpleTerminal(args.toList()).run()
        } else {
          demoLog.info { "${KattyUtils.threadName()}: JvmMain.main() coroutine" }
          demoMain(args)
        }
      }
    }
  }
}