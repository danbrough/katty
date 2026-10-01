package io.github.danbrough.katty.demos

import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.globalSession
import kotlinx.coroutines.currentCoroutineContext

val ContextDemo = basicCommand("context", "Context demos") {
  success("ran context demo")
  globalSession {
    val ctx1 = currentCoroutineContext()
    currentCoroutineContext().fold(Unit) { _, ctx ->
      success("coroutine context: $ctx")
    }
    globalSession {
      currentCoroutineContext().fold(Unit) { _, ctx ->
        info("coroutine context: $ctx")
      }
      info("ctx1: $ctx1 == current: ${currentCoroutineContext()} is ${ctx1 == currentCoroutineContext()}")

      globalSession {
        currentCoroutineContext().fold(Unit) { _, ctx ->
          muted("coroutine context: $ctx")
        }
        muted("ctx1: $ctx1 == current: ${currentCoroutineContext()} is ${ctx1 == currentCoroutineContext()}")
      }
    }
  }
}