package io.github.danbrough.katty

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking


actual fun ktorClientFactory(): HttpClientEngineFactory<*> = CIO
actual fun HttpClientConfig<HttpClientEngineConfig>.clientConfig() {
}

class JvmMain {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      logDemo.info { "${KattyUtils.threadName()}: in JvmMain.main()" }
      runBlocking {
        if (args.getOrNull(0) == "simple") {
          SimpleTerminal(args.toList()).run()
        } else {
          logDemo.info { "${KattyUtils.threadName()}: JvmMain.main() coroutine" }
          demoMain(args)
        }
      }
    }
  }
}