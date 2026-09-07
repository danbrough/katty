package io.github.danbrough.katty

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

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
}
