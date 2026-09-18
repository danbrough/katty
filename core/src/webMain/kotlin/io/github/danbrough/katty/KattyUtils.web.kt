@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.danbrough.katty

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
  actual val ioDispatcher: CoroutineDispatcher
    get() = Dispatchers.Default

  actual fun atExit(block: () -> Unit) {
    error("atExit not implemented")
    /*
    //on nodejs ..
    process.on("exit") { code ->
          println("Process exiting with code $code. Cleaning up...")
        }

        //for web browsers ...
        import kotlinx.browser.window

    fun main() {
        // 1. For saving state right before the user leaves or closes the tab
        window.addEventListener("beforeunload", { event ->
            // Perform fast, synchronous cleanup here
            saveTemporaryData()

            // Optional: Modern browsers require this to show a confirmation dialog
            // event.preventDefault()
        })

        // 2. Alternatively, use 'unload' for guaranteed final cleanup
        window.addEventListener("unload", {
            flushRemainingLogs()
        })
    }

    fun saveTemporaryData() {
        window.localStorage.setItem("session_backup", "your_data")
    }


        */
  }
}


