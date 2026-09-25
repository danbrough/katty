package io.github.danbrough.katty.demos

import io.github.danbrough.katty.CommandExecutor
import io.github.danbrough.katty.CommandExecutor.CommandContext.Companion.withCommandContext
import io.github.danbrough.katty.KTerminal
import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.demoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private val log = demoLog



interface TestScope<T> : CoroutineContext.Element, AutoCloseable

class TopScope : TestScope<TopScope> {

  companion object : CoroutineContext.Key<TopScope> {
    val INSTANCE: TopScope = TopScope()
  }


  init {
    log.info { "TopScope::init" }
    /*    KattyUtils.atExit {
          if (INSTANCE.isInitialized())
            INSTANCE.value.close()
        }*/
  }

  override val key: CoroutineContext.Key<*> = TopScope


  override fun close() {
    log.info { "TopScope::close" }
  }
}


class ContextMessage(val message: String) : CoroutineContext.Element , AutoCloseable{
  companion object : CoroutineContext.Key<ContextMessage>

  override val key: CoroutineContext.Key<*> = ContextMessage

  override fun close() {
    log.trace { "ContextMessage::close() message:$message" }
  }
}

suspend fun <R> topScope(block: suspend TopScope.() -> R) {
  CommandExecutor.getOrCreate(TopScope) {
    TopScope.INSTANCE
  }.block()
}


private var count = 1

@OptIn(ExperimentalCoroutinesApi::class)
val scopeDemo =
  basicCommand("scopeDemo", "Demos how to manage scopes. args = [session,message,clear,test]") { args ->
    println("args: ${args.joinToString(",")} job: ${currentCoroutineContext().job}")
    when (args[1]) {
      "session" -> {
        log.info { "scopeDemo::session" }
        topScope {
          val scope = currentCoroutineContext()[KTerminal.RawModeContext]?.scope
          log.info { "scopeDemo::topScope start. scope: $scope job: ${currentCoroutineContext().job} parent:${currentCoroutineContext().job.parent}" }
          topScope {
            log.info { "scopeDemo::inside second top scope" }
          }

          val executor = currentCoroutineContext()[CommandExecutor]!!

          val commandScope = executor.scope
          log.debug { "commandScope: $commandScope" }
          log.debug { "commandScopeContext: ${commandScope.coroutineContext}" }
        }
      }

      "message" -> {
        log.info { "scopeDemo::message ${currentCoroutineContext()[ContextMessage]?.message}" }
        CommandExecutor += ContextMessage("message ${count++}")
      }

      "clear" -> {
        CommandExecutor -= ContextMessage
      }

      "test" -> {
        withCommandContext(ContextMessage("Context message created at ${Clock.System.now()}")) {
          runTest()
        }
      }

      else -> {}
    }
  }

private suspend fun runTest(){
  val message = currentCoroutineContext()[ContextMessage]?.message
  log.info { "runTest() message: $message" }
  for(n in 1 .. 10) {
    delay(1.seconds)
    log.info { "runTest():$n still running with message: $message" }
  }
}