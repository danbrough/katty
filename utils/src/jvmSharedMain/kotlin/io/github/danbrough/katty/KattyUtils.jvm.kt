package io.github.danbrough.katty

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.net.Inet4Address
import java.net.Inet6Address
import kotlin.concurrent.thread

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object KattyUtils {
  actual fun getEnv(name: String): String? = System.getenv(name)

  actual fun exec(command: List<String>): Source {
    val process = ProcessBuilder(command.drop(1)).start()
    return process.inputStream.asSource().buffered()
  }

  actual fun threadName(): String = Thread.currentThread().name
  actual val ioDispatcher: CoroutineDispatcher
    get() = Dispatchers.IO

  actual fun atExit(block: () -> Unit) {
    println("atExit: Adding shutdown hook.... ")
    Runtime.getRuntime().addShutdownHook(thread(start = false, block = block))
  }

  actual fun resolveHostName(hostName: String): List<String> {

    if (IPAddressValidator.isIPAddress(hostName)) return listOf(hostName)
    val addresses = mutableListOf<String>()

    runCatching {
      addresses.addAll(Inet4Address.getAllByName(hostName).toList().map { it.hostAddress })
    }
    runCatching {
      Inet6Address.getAllByName(hostName).toList().map { it.hostAddress }.forEach { address ->
        if (!addresses.contains(address)) addresses.add(address)
      }
    }
    return addresses
  }
}
