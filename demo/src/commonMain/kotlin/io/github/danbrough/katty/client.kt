package io.github.danbrough.katty

import io.github.danbrough.katty.client.WsClient
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.messages.KattyMessage
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.decodeFromByteArray

private val log = logDemo

private const val USAGE = "client [start|stop|message]"
fun demoClientCommand() =
  basicCommand("client", USAGE) {
    val args = it.drop(1)
    val cmd = args.getOrNull(0) ?: run {
      warn(USAGE)
      return@basicCommand
    }

    fun createWsClient() = WsClient(shellScope, "http://localhost:8080/ws", cbor = cbor) {
      for (frame in incoming) {
        info("received frame: $frame")

        when (frame) {
          is Frame.Text -> {
            muted("text frame: [${frame.readText()}]")
          }

          is Frame.Binary -> {
            val msg = cbor.decodeFromByteArray<KattyMessage>(frame.data)
            info("received msg: $msg")
          }

          is Frame.Ping -> log.trace { "ping frame" }
          is Frame.Pong -> log.trace { "pong frame" }
          is Frame.Close -> log.info { "close frame" }
          else -> {}
        }
      }
    }

    when (cmd) {
      "start" -> shellContext(WsClient, ::createWsClient)

      "stop" -> shellContextRemove(WsClient)

      "message" -> {
        val msg = args.getOrNull(1) ?: "Hello from WsClient"
        shellContext(WsClient, ::createWsClient).enqueueMessage(StringMessage(msg))
      }
    }
  }



