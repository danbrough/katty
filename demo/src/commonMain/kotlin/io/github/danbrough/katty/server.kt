package io.github.danbrough.katty

import io.github.danbrough.katty.server.KattyServer
import io.github.danbrough.katty.server.commandServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

val DemoServerCommand = commandServer {
  KattyServer {
    routing {
      get("/") {
        call.respondText {
          "Demo server working at ${
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
          }  thread:${KattyUtils.threadName()} coroutine:${currentCoroutineContext()[CoroutineName]}\nTerminal: ${currentCoroutineContext()[KattyShell]}\n"
        }
      }
    }
  }
}