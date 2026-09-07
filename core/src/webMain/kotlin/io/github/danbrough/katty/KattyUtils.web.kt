@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.danbrough.katty

import kotlinx.io.Source

private fun jsPrintln(s: String?) {
  js("console.info(s)")
}

@Suppress("RedundantNullableReturnType")
private fun getEnvJS(name: String): String? =
  js("typeof process === 'object' ? process.env[name] : null")

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual object KattyUtils {
  actual fun getEnv(name: String): String? = getEnvJS(name)

  actual fun exec(command: List<String>): Source {
    TODO("exec not implemented for web/JS")
  }

  actual fun threadName(): String = "Web"
}


