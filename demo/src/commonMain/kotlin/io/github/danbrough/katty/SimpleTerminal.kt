package io.github.danbrough.katty

import com.github.ajalt.mordant.input.coroutines.receiveKeyEventsFlow
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.SystemLineSeparator
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val log = demoLog


private typealias SimpleCommand = suspend SimpleTerminal.(args: List<String>) -> Unit

class SimpleTerminal(val args: List<String>) {
  val terminal = Terminal()

  private val supervisorJob = SupervisorJob()
  private val cmdScope = CoroutineScope(supervisorJob)

  suspend fun test(args: List<String>) {
    println("test[${KattyUtils.threadName()}  args: $args")
    cmdScope.launch(Dispatchers.Main) {
      println("test[${KattyUtils.threadName()}  launched coroutine")
      for(n in 1..10) {
        delay(1.seconds)
        println("test[${KattyUtils.threadName()}] n = $n")
      }
    }
    yield()
  }

  val commands = mapOf<String, SimpleCommand>("date" to {
    println(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString())
  }, "test" to { test(it) })


  suspend fun run() {
    log.trace { "SimpleTerminal::run()" }

    val command = StringBuilder()
    val prompt = "$ "
    var pos = prompt.length

    fun printPrompt() {
      print(prompt)
      pos = prompt.length
      command.clear()
    }

    printPrompt()

    suspend fun processKeyEvent(e: com.github.ajalt.mordant.input.KeyboardEvent) {
      if (pos == 0) printPrompt()
      if (e.isCtrlD || e.isCtrlC) throw KattyException.ExitException()
      if (e.key == "Enter") {
        val cmd = command.toString()
        if (cmd.isNotBlank()) {
          println()
          pos = 0
          commands[cmd]?.invoke(this@SimpleTerminal, emptyList())
            ?: println("${SystemLineSeparator}You entered: $cmd")
        }
        printPrompt()
      } else if (e.key.length == 1) {
        print(e.key)
        command.append(e.key)
        pos++
      }
    }

    runCatching {
      terminal.enterRawMode().use { scope->
        while(true) {
          scope.readKeyOrNull(50.milliseconds)?.also { e ->
            processKeyEvent(e)
          }
          yield()
        }
      }
    }.exceptionOrNull()?.also {
      println("ERROR: ${it.message}")
      printPrompt()
    }
  }
}