package io.github.danbrough.katty

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.RawModeScope
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.SystemLineSeparator
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val log = demoLog


private typealias SimpleCommand = suspend SimpleTerminal.(args: List<String>) -> Unit

class SimpleTerminal(val args: List<String>) : CoroutineContext.Element {
  companion object : CoroutineContext.Key<SimpleTerminal>

  override val key: CoroutineContext.Key<*> = SimpleTerminal
  val terminal = Terminal()

  private val supervisorJob = SupervisorJob()
  private val cmdScope = CoroutineScope(supervisorJob)

  val commands = mapOf<String, SimpleCommand>(
    "date" to {
      println(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString())
    },
    "test" to { test(cmdScope, it) },
    "message" to { message(cmdScope, it) },
    "printMessage" to {
      success(
        currentCoroutineContext()[ContextMessage]?.message ?: "no ContextMessage"
      )
    })

  val command = StringBuilder()
  val prompt = "$ "
  var pos = 0

  suspend fun printPrompt(scope: RawModeScope) {
    if (pos == 0) {
      print(prompt)
      pos = prompt.length
      command.clear()
    }
    processNextKey(scope)
  }

  fun print(str: String, style: TextStyle = terminal.theme.info) = terminal.print(str.let {
    pos += it.length
    style(it)
  })

  fun println() {
    pos = 0
    terminal.println()
  }

  fun println(str: String, style: TextStyle = terminal.theme.info) =
    print(str + SystemLineSeparator, style).also { pos = 0 }

  fun info(str: String, style: TextStyle = terminal.theme.info) = println(str, style)
  fun success(str: String, style: TextStyle = terminal.theme.success) = println(str, style)
  fun muted(str: String, style: TextStyle = terminal.theme.muted) = println(str, style)
  fun warning(str: String, style: TextStyle = terminal.theme.warning) = println(str, style)
  fun danger(str: String, style: TextStyle = terminal.theme.danger) = println(str, style)


  suspend fun processKeyEvent(e: KeyboardEvent, scope: RawModeScope) {
    if (e.isCtrlD || e.isCtrlC) throw Errors.ExitException()
    if (e.key == "Enter") {
      val cmd = CommandLine.parseCommandLineArgs(command.toString().trim())
      //log.trace { "enter pressed cmd: [${cmd.joinToString(",")}]" }
      if (cmd.isNotEmpty()) {
        command.clear()
        println()
        commands[cmd[0]]?.invoke(this@SimpleTerminal, cmd)
          ?: println("${SystemLineSeparator}You entered: $cmd")
      } else printPrompt(scope)
    } else if (e.key.length == 1) {
      print(e.key)
      command.append(e.key)
      pos++
      processNextKey(scope)
    } else processNextKey(scope)
  }


  suspend fun processNextKey(scope: RawModeScope) {
    while (true) {
      scope.readKeyOrNull(50.milliseconds)?.also { e ->
        processKeyEvent(e, scope)
        break
      } ?: yield()
    }
  }

  suspend fun run() {
    log.trace { "SimpleTerminal::run()" }
    withContext(this) {
      while (true) {
        runCatching {
          rawMode { scope ->
            printPrompt(scope)
          }
        }.exceptionOrNull()?.also {
          if (it is Errors.ExitException) break
          println("ERROR: ${it.message}")
          run()
        }
      }
    }
  }
}

private class RawModeContext(val scope: RawModeScope) : CoroutineContext.Element, AutoCloseable {
  companion object : CoroutineContext.Key<RawModeContext>

  override val key: CoroutineContext.Key<*> = RawModeContext

  override fun close() = scope.close()
}

private suspend fun SimpleTerminal.rawMode(block: suspend (RawModeScope) -> Unit) =
  currentCoroutineContext()[RawModeContext]?.scope?.let { block(it) }
    ?: RawModeContext(terminal.enterRawMode()).use { scopeContext ->
      withContext(scopeContext) {
        block(scopeContext.scope)
      }
    }

class ContextMessage(val message: String) : CoroutineContext.Element {
  companion object : CoroutineContext.Key<ContextMessage>

  override val key: CoroutineContext.Key<*> = ContextMessage
}

suspend fun SimpleTerminal.test(cmdScope: CoroutineScope, args: List<String>) {
  println("test[${KattyUtils.threadName()}  args: $args")
  cmdScope.launch(Dispatchers.Default) {
    println("test[${KattyUtils.threadName()}  launched coroutine")
    for (n in 1..10) {
      delay(1.seconds)
      println("test[${KattyUtils.threadName()}] n = $n")
    }
  }
  printPrompt(currentCoroutineContext()[RawModeContext]!!.scope)
}

private var count = 1
suspend fun SimpleTerminal.message(cmdScope: CoroutineScope, args: List<String>) {
  val ctx = currentCoroutineContext()
  info("current message: ${ctx[ContextMessage]?.message} args: [${args.joinToString(",")}]")
  val msg = ContextMessage("ContextMessage: ${count++}")

  muted("new msg: ${msg.message}")
  withContext(msg) {
    printPrompt(currentCoroutineContext()[RawModeContext]!!.scope)
  }
  //processNextKey(currentCoroutineContext()[RawModeContext]!!.scope)
}