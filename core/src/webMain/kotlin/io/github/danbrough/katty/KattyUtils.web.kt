@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.danbrough.katty

private fun jsPrintln(s: String?) {
  js("console.info(s)")
}

@Suppress("RedundantNullableReturnType")
private fun getEnvJS(name: String): String? =
  js("typeof process === 'object' ? process.env[name] : null")

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual object KattyUtils : io.github.danbrough.katty.Utils {
  actual override fun getEnv(name: String): String? = getEnvJS(name)
}

