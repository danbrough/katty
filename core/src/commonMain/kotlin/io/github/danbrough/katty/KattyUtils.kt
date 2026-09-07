package io.github.danbrough.katty

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.io.Source


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object KattyUtils {
  fun getEnv(name: String): String?
  fun exec(command: List<String>): Source

  fun threadName():String

  val ioDispatcher: CoroutineDispatcher
}


