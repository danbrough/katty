package io.github.danbrough.katty

import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object KattyUtils  {
  actual  fun getEnv(name: String): String? = System.getenv(name)

  actual fun exec(command: String): Source {
    val process = ProcessBuilder("sh", "-c", command).start()
    return process.inputStream.asSource().buffered()
  }
}
