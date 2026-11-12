package io.github.danbrough.katty.client

import io.github.danbrough.katty.Action
import io.github.danbrough.katty.ActionMessage
import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.WsMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

private val log = logClient

class KattyClient(override val key: CoroutineContext.Key<*> = KattyClient) : KattyContext {

  companion object : CoroutineContext.Key<KattyClient>


  // 1. Initialize the Ktor client and install WebSocket capabilities
  private val client = HttpClient {
    install(WebSockets) {
      pingIntervalMillis = 15_000 // Send app-level ping frames every 15s
    }
  }

  private var session: DefaultClientWebSocketSession? = null

  /**
   * Connects to the Ktor CBOR WebSocket server and opens a session
   */
  suspend fun connectAndListen(host: String = "localhost", port: Int = 8080) {
    try {
      client.webSocket(host = host, port = port, path = "/ws") {
        log.info { "Connected to CBOR WebSocket Server successfully." }
        session = this

        // Launch a separate concurrent coroutine job to listen for incoming packets
        val receiveJob = launch {
          try {
            for (frame in incoming) {
              if (frame is Frame.Binary) {
                val rawBytes = frame.readBytes()

                // Deserialize raw CBOR bytes directly into our common data model
                val serverResponse: WsMessage = Cbor.decodeFromByteArray(rawBytes)
                log.debug { "Received from server wsMessage: $serverResponse" }
              } else {
                log.trace { "Received from server frame: $frame" }
                when (frame) {
                  is Frame.Text -> log.trace { "FRAME TEXT: [${frame.readText()}]" }
                  is Frame.Binary -> log.trace { "FRAME BINARY" }
                  is Frame.Ping -> log.trace { "FRAME PING" }
                  is Frame.Pong -> log.trace { "FRAME PONG" }
                  is Frame.Close -> log.trace { "FRAME CLOSE" }
                }
              }
            }
          } catch (e: Exception) {
            log.error(e) { "Error while receiving frames: ${e.message}" }
          }
        }

        // 2. Demo: Send an initial CBOR binary payload to the server
        val initialMessage: WsMessage = ActionMessage(
          action = Action.LOGIN,
          payload = "Hello from a KMP Multiplatform Client!",
        )

        sendCborMessage(initialMessage)

        // Keep the socket session block alive as long as the connection is running
        receiveJob.join()
      }
    } catch (e: Exception) {
      log.error { "WebSocket Connection Error: ${e.message}" }
    } finally {
      log.info { "WebSocket session terminated." }
    }
  }

  /**
   * Helper extension function inside the DefaultClientWebSocketSession scope
   * to smoothly abstract away the CBOR serialization details.
   */
  suspend fun sendCborMessage(message: WsMessage) {
    var wsSession = session
    while (isActive && wsSession == null) {
      delay(1.seconds)
      wsSession = session
    }

    if (isActive && wsSession != null) {
      // Encode our common object directly into a serialized byte stream
      val binaryData = Cbor.encodeToByteArray<WsMessage>(message)

      // Dispatch a standard Binary web-socket frame down the pipe
      wsSession.send(Frame.Binary(fin = true, data = binaryData))
      log.debug { "Sent message:  [$message] to server." }
    }
  }

  override fun close() {
    log.info { "KattyClient::close()" }
    client.close()
  }
}