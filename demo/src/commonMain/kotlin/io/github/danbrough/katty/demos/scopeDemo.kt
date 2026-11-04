package io.github.danbrough.katty.demos

import io.github.danbrough.katty.CommandContext.Companion.withCommandContext
import io.github.danbrough.katty.ShellContext
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextOrNull
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private val log = logDemo


private var count = 1

private const val USAGE =
  "Demos how to manage scopes. args = [session,message,shell,clear,test,thang]"
val scopeDemo = basicCommand(
  "scopeDemo",
  USAGE,
) { args ->
  println("args: ${args.joinToString(",")} job: ${currentCoroutineContext().job}")
  val cmd = args.getOrNull(1) ?: run {
    info(USAGE)
    return@basicCommand
  }


  when (args[1]) {
    "shell" -> {
      log.debug { "shellCOntext:${shellContextOrNull()}" }
    }

    "session" -> {
      log.info { "scopeDemo::session" }
    }

    "message" -> {
      log.info { "scopeDemo::message ${shellContext(ContextMessage)?.message ?: "not yet set"}" }


      ShellContext.add(ContextMessage("message ${count++}"))

      log.info { "context message is now: ${shellContext(ContextMessage)}" }
    }

    "clear" -> {
      ShellContext.remove(ContextMessage)
    }

    "test" -> {
      withCommandContext(ContextMessage("Context message created at ${Clock.System.now()}")) {
        runTest()
      }
    }

    "thang" -> {
      warn("not implemented")/*    val ctx = currentCoroutineContext()
            log.trace { "scopeDemo::thang ctx: $ctx ctx.thang = ${ctx[Thang]}" }
            shellContext() += Thang("Thang_${count++}")*/
    }

    else -> {}
  }
}

private suspend fun runTest() {
  val message = currentCoroutineContext()[ContextMessage]?.message
  log.info { "runTest() message: $message" }
  for (n in 1..10) {
    delay(1.seconds)
    log.info { "runTest():$n still running with message: $message" }
  }
}

