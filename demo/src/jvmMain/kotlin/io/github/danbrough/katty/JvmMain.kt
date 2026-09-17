package io.github.danbrough.katty

import kotlinx.coroutines.runBlocking

class JvmMain {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      runBlocking {
        demoMain(args)
      }
    }
  }
}