package io.github.danbrough.katty

import io.github.danbrough.katty.server.KattyServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


private val log = logDemo
suspend fun serverTest() {
  log.info { "running serverTest" }

  val server = KattyServer {
    routing {
      get("/") {
        call.respondText(
          "The server is running at ${
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
          }"
        )
      }
    }
  }

  server.start()
}