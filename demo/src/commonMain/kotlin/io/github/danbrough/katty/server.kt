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

val DemoServerCommand = commandServer { args ->

  info(
    """
      starting katty webserver.. args: $args terminal: ${kattyShellOrNull()}
      shellContext: ${shellContextOrNull()}
      
  """.trimIndent()
  )

  KattyServer(shellContext()) {
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
            
          """.trimIndent()
        }
      }
    }
  }

}