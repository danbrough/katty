package io.github.danbrough.katty.demos

import io.github.danbrough.katty.CommandContext.Companion.withCommandContext
import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.demoLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private val log = demoLog


class ContextMessage(val message: String) : CoroutineContext.Element, AutoCloseable {
  companion object : CoroutineContext.Key<ContextMessage>

  override val key: CoroutineContext.Key<*> = ContextMessage

  override fun close() {
    log.trace { "ContextMessage::close() message:$message" }
  }
}


private var count = 1

@OptIn(ExperimentalCoroutinesApi::class)
val scopeDemo =
  basicCommand(
    "scopeDemo",
    "Demos how to manage scopes. args = [session,message,clear,test]"
  ) { args ->
    println("args: ${args.joinToString(",")} job: ${currentCoroutineContext().job}")
    when (args[1]) {
      "session" -> {
        log.info { "scopeDemo::session" }
      }

      "message" -> {
        log.info { "scopeDemo::message ${currentCoroutineContext()[ContextMessage]?.message}" }
        //CommandExecutor += ContextMessage("message ${count++}")
      }

      "clear" -> {
        //CommandExecutor -= ContextMessage
      }

      "test" -> {
        withCommandContext(ContextMessage("Context message created at ${Clock.System.now()}")) {
          runTest()
        }
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