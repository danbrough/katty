@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.danbrough.katty

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.io.Source


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object KattyUtils {
  fun getEnv(name: String): String?
  fun exec(command: List<String>): Source

  fun threadName(): String

  val ioDispatcher: CoroutineDispatcher

  fun atExit(block: () -> Unit): Unit
}



