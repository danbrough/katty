package io.github.danbrough.katty.client

import io.github.danbrough.katty.WsMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.time.Clock

class KattyClient : AutoCloseable {

  // 1. Initialize the Ktor client and install WebSocket capabilities
  private val client = HttpClient {
    install(WebSockets) {
      pingIntervalMillis = 15_000 // Send app-level ping frames every 15s
    }
  }

  /**
   * Connects to the Ktor CBOR WebSocket server and opens a session
   */
  @OptIn(ExperimentalSerializationApi::class)
  suspend fun connectAndListen(host: String = "localhost", port: Int = 8080) {
    try {
      client.webSocket(host = host, port = port, path = "/ws/cbor") {
        println("Connected to CBOR WebSocket Server successfully.")

        // Launch a separate concurrent coroutine job to listen for incoming packets
        val receiveJob = launch {
          try {
            for (frame in incoming) {
              if (frame is Frame.Binary) {
                val rawBytes = frame.readBytes()

                // Deserialize raw CBOR bytes directly into our common data model
                val serverResponse = Cbor.decodeFromByteArray<WsMessage>(rawBytes)
                println("Received from server: [${serverResponse.action}] -> ${serverResponse.payload}")
              }
            }
          } catch (e: Exception) {
            println("Error while receiving frames: ${e.message}")
          }
        }

        // 2. Demo: Send an initial CBOR binary payload to the server
        val initialMessage = WsMessage(
          action = "USER_LOGIN",
          payload = "Hello from a KMP Multiplatform Client!",
          timestamp = Clock.System.now().toEpochMilliseconds()
        )

        sendCborMessage(initialMessage)

        // Keep the socket session block alive as long as the connection is running
        receiveJob.join()
      }
    } catch (e: Exception) {
      println("WebSocket Connection Error: ${e.message}")
    } finally {
      println("WebSocket session terminated.")
    }
  }

  /**
   * Helper extension function inside the DefaultClientWebSocketSession scope
   * to smoothly abstract away the CBOR serialization details.
   */
  @OptIn(ExperimentalSerializationApi::class)
  private suspend fun DefaultClientWebSocketSession.sendCborMessage(message: WsMessage) {
    if (isActive) {
      // Encode our common object directly into a serialized byte stream
      val binaryData = Cbor.encodeToByteArray(message)

      // Dispatch a standard Binary web-socket frame down the pipe
      send(Frame.Binary(fin = true, data = binaryData))
      println("Sent binary action [${message.action}] to server.")
    }
  }

  override fun close() {
    logClient.info { "KattyClient::close()" }
    client.close()
  }
}