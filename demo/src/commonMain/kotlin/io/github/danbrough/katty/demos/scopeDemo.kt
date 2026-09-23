package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.demoLog
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

private val log = demoLog


interface TestScope<T> : CoroutineContext.Element, AutoCloseable

class TopScope : TestScope<TopScope> {

  companion object : CoroutineContext.Key<TopScope> {
    val INSTANCE: Lazy<TopScope> = lazy { TopScope() }
  }


  init {
    log.info { "TopScope::init" }
    KattyUtils.atExit {
      if (INSTANCE.isInitialized())
        INSTANCE.value.close()
    }
  }

  override val key: CoroutineContext.Key<*> = TopScope


  override fun close() {
    log.info { "TopScope::close" }
  }

}

suspend fun <R> topScope(block: suspend TopScope.() -> R) {
  currentCoroutineContext()[TopScope]?.block() ?: TopScope.INSTANCE.value.run {
    withContext(this){
      block()
    }
  }
}

val scopeDemo = basicCommand("scopeDemo", "Demos how to manage scopes. args = [session]") { args ->
  println("args: ${args.joinToString(",")}")
  when (args[1]) {
    "session" -> {
      log.info { "scopeDemo::session" }
      topScope {
        log.info { "scopeDemo::topScope start" }
      }
    }

    else -> {}
  }
}