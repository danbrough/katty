package io.github.danbrough.katty

import io.github.danbrough.katty.messages.OK
import io.github.danbrough.katty.messages.asBinary
import io.github.danbrough.katty.server.KattyServer
import io.github.danbrough.katty.server.serverCommand
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val log = logDemo
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


fun Application.wsShell(
  path: String = KattyServer.DEFAULT_WEBSOCKET_PATH
) = routing {
  webSocket(path) {
    log.info { "wsShell() new connection .." }
    try {

      send(Frame.Binary(fin = true, OK.asBinary(cbor)))

      for (frame in incoming) {
        log.trace { "wsShell::received frame: $frame" }
        when (frame) {
          is Frame.Binary -> {
            val rawBytes = frame.readBytes()

            /*            // Parse CBOR payload into shared common metadata model
                        val incomingMsg: WsMessage = Cbor.decodeFromByteArray<WsMessage>(rawBytes)
                        log.trace { "Processing Action: $incomingMsg" }

                        // Formulate response object
                        val acknowledgment = ActionMessage(
                          Action.LOGIN_ACK,
                          payload = "Processed successfully",
                        )

                        // Serialize object to binary array and emit back down the pipeline
                        val responseBytes = Cbor.encodeToByteArray<WsMessage>(acknowledgment)
                        send(Frame.Binary(fin = true, data = responseBytes))*/
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