package io.github.danbrough.katty.server

import io.github.danbrough.katty.ShellContext
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext

suspend fun Application.wsShell(
  path: String = KattyServer.DEFAULT_WEBSOCKET_PATH
) = routing {
  webSocket(path) {
    send("Connected to katty server! name:${currentCoroutineContext()[CoroutineName]}")

    for (frame in incoming) {
      // Handle only Text frames, ignore others
      frame as? Frame.Text ?: continue
      val receivedText = frame.readText()

      if (receivedText.equals("bye", ignoreCase = true)) {
        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
      } else {
        send(Frame.Text("Hi, $receivedText!"))
      }
    }
  }
}