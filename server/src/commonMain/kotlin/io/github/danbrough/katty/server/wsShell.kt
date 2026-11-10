package io.github.danbrough.katty.server

import io.github.danbrough.katty.Action
import io.github.danbrough.katty.ActionMessage
import io.github.danbrough.katty.WsMessage
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

private val log = logServer


fun Application.wsShell(
  path: String = KattyServer.DEFAULT_WEBSOCKET_PATH
) = routing {
  webSocket(path) {
    log.info { "wsShell() new connection .." }
    try {
      for (frame in incoming) {
        when (frame) {
          is Frame.Binary -> {
            val rawBytes = frame.readBytes()

            // Parse CBOR payload into shared common metadata model
            val incomingMsg: WsMessage = Cbor.decodeFromByteArray<WsMessage>(rawBytes)
            log.trace { "Processing Action: $incomingMsg" }

            // Formulate response object
            val acknowledgment = ActionMessage(
              Action.LOGIN_ACK,
              payload = "Processed successfully",
            )

            // Serialize object to binary array and emit back down the pipeline
            val responseBytes = Cbor.encodeToByteArray<WsMessage>(acknowledgment)
            send(Frame.Binary(fin = true, data = responseBytes))
          }

          is Frame.Text -> {
            // Graceful handling if text is received but binary is required
            val textMsg = frame.readText()
            log.warn { "Unexpected Text Frame: $textMsg. CBOR requires Binary frames." }
          }

          else -> { /* Handle structural frames like Close, Ping, Pong implicitly */
            log.trace { "Received frame: $frame" }
          }
        }
      }
    } catch (e: Exception) {
      println("WebSocket Session Exception: ${e.message}")
    } finally {
      println("WebSocket session cleaned up. Client disconnected.")
    }
  }
}
