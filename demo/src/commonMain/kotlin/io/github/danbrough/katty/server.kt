package io.github.danbrough.katty

import io.github.danbrough.katty.server.KattyServer
import io.github.danbrough.katty.server.serverCommand
import io.github.danbrough.katty.server.wsShell
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun demoServerCommand() = serverCommand { args ->

  info(
    """
      starting katty webserver.. args: $args    
  """.trimIndent()
  )
  KattyServer(Message("The Server Scope") + shellScope.coroutineContext) {
    wsShell()
    routing {
      get("/") {
        call.respondText {
          """
            Welcome to the demo server at ${
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
          }
            thread: ${KattyUtils.threadName()} coroutineName: ${currentCoroutineContext()[CoroutineName]}
            shellContext: ${shellContextOrNull()}
            terminal: ${kattyShellOrNull()}
            message: ${currentCoroutineContext()[Message]}
            
          """.trimIndent()
        }
      }
    }
  }

}