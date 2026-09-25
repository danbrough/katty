package io.github.danbrough.katty

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
suspend fun main(args: Array<String>) {
  println("running node js demo .. args: ${args.joinToString()}")
  val job = coroutineScope {
    launch {
      //demoMain(args)
      SimpleTerminal(args.toList()).run()
    }
  }
  job.join()
}

// Native Node.js external bindings
external val process: dynamic
external fun require(module: String): dynamic

fun jsTest(args: Array<String>) {
  val readline = require("node:readline")
  val stdin = process.stdin
  val stdout = process.stdout

  // 1. Prepare stdin to emit single keypress events
  readline.emitKeypressEvents(stdin)


  // 2. Enable raw mode so inputs are sent immediately
  if (stdin.isTTY == true) {
    stdin.setRawMode(true)
  }

  // 3. Prevent the process from exiting immediately
  stdin.resume()

  println("Press any key... (Press Ctrl+C to exit)")

  // 4. Non-blocking listener using a Kotlin lambda
  stdin.on("keypress") { chunk: Any?, key: dynamic ->
    // Since raw mode overrides Ctrl+C, handle termination manually
    if (key != null && key.ctrl == true && key.name == "c") {
      process.exit()
    }

    println("\nYou pressed: \"$chunk\"")
    // Optional: print structural details of the key
    // println("Key metadata: name=${key?.name}, shift=${key?.shift}")
  }

  // Demonstration that the app isn't blocked:
  // This background interval will keep printing dots smoothly
  js("setInterval(function() { process.stdout.write('.'); }, 1000)")
}
