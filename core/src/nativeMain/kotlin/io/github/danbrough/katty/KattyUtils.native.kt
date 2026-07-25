@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.danbrough.katty

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

actual object KattyUtils : Utils {
  @OptIn(ExperimentalForeignApi::class)
  actual override fun getEnv(name: String): String? = getenv(name)?.toKString()
}