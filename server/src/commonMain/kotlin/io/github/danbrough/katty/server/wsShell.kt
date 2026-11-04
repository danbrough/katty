package io.github.danbrough.katty.server

import io.github.danbrough.katty.WsMessage
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.time.Clock

suspend fun Application.wsShellOld(
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

@OptIn(ExperimentalSerializationApi::class)
suspend fun Application.wsShell(
  path: String = KattyServer.DEFAULT_WEBSOCKET_PATH
) = routing {
  webSocket(path) {
    send("Connected to katty server! name:${currentCoroutineContext()[CoroutineName]}")
    try {
      for (frame in incoming) {
        when (frame) {
          is Frame.Binary -> {
            val rawBytes = frame.readBytes()

            // Parse CBOR payload into shared common metadata model
            val incomingMsg = Cbor.decodeFromByteArray<WsMessage>(rawBytes)
            println("Processing Action: ${incomingMsg.action}")

            // Formulate response object
            val acknowledgment = WsMessage(
              action = "ACK_${incomingMsg.action}",
              payload = "Processed successfully",
              timestamp = Clock.System.now().epochSeconds
            )

            // Serialize object to binary array and emit back down the pipeline
            val responseBytes = Cbor.encodeToByteArray(acknowledgment)
            send(Frame.Binary(fin = true, data = responseBytes))
          }

          is Frame.Text -> {
            // Graceful handling if text is received but binary is required
            val textMsg = frame.readText()
            println("Unexpected Text Frame: $textMsg. CBOR requires Binary frames.")
          }

          else -> { /* Handle structural frames like Close, Ping, Pong implicitly */
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

/*
// src/jvmMain/kotlin/Server.kt
package com.example

import com.example.models.WsMessage
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = 8080) {
        // Install WebSockets plugin to support ws:// and wss:// communication
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        routing {
            webSocket("/ws/cbor") {
                println("Client established a KMP WebSocket connection.")

                try {
                    // Loop continuously suspends as long as the socket stays connected
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Binary -> {
                                val rawBytes = frame.readBytes()

                                // Parse CBOR payload into shared common metadata model
                                val incomingMsg = Cbor.decodeFromByteArray<WsMessage>(rawBytes)
                                println("Processing Action: ${incomingMsg.action}")

                                // Formulate response object
                                val acknowledgment = WsMessage(
                                    action = "ACK_${incomingMsg.action}",
                                    payload = "Processed successfully",
                                    timestamp = System.currentTimeMillis()
                                )

                                // Serialize object to binary array and emit back down the pipeline
                                val responseBytes = Cbor.encodeToByteArray(acknowledgment)
                                send(Frame.Binary(fin = true, data = responseBytes))
                            }
                            is Frame.Text -> {
                                // Graceful handling if text is received but binary is required
                                val textMsg = frame.readText()
                                println("Unexpected Text Frame: $textMsg. CBOR requires Binary frames.")
                            }
                            else -> { /* Handle structural frames like Close, Ping, Pong implicitly */ }
                        }
                    }
                } catch (e: Exception) {
                    println("WebSocket Session Exception: ${e.localizedMessage}")
                } finally {
                    println("WebSocket session cleaned up. Client disconnected.")
                }
            }
        }
    }.start(wait = true)
}

 */