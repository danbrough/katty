package io.github.danbrough.katty

import io.github.danbrough.katty.client.WsClient
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.messages.KattyMessage
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

private val log = logDemo


suspend fun DefaultWebSocketSession.sendMessage(m: KattyMessage) =
  send(Frame.Binary(fin = true, CborFrameCodec.encode(m)))

expect fun ktorClientFactory(): HttpClientEngineFactory<*>
expect fun HttpClientConfig<HttpClientEngineConfig>.clientConfig()

private const val USAGE = "client [start|stop|message]"
fun demoClientCommand() = basicCommand("client", USAGE) {
  val args = it.drop(1)
  val cmd = args.getOrNull(0) ?: run {
    warn(USAGE)
    return@basicCommand
  }


  fun createWsClient(url: String = "http://localhost:8080/ws") = WsClient(
    shellContext.scope,
    url,
    config = { clientConfig() },
    clientEngine = ktorClientFactory(),
    codec = CborFrameCodec,
  ) { wsClient ->
    for (frame in incoming) {
      info("received frame: $frame")

      when (frame) {
        is Frame.Text -> {
          muted("text frame: [${frame.readText()}]")
        }

        is Frame.Binary -> {
          val msg = CborFrameCodec.decode<KattyMessage>(frame.data)
          info("received msg: $msg")
          wsClient.onResponseReceived(msg)
        }

        is Frame.Ping -> log.trace { "ping frame" }
        is Frame.Pong -> log.trace { "pong frame" }
        is Frame.Close -> log.info { "close frame" }
        else -> {}
      }
    }
  }

  when (cmd) {
    "start" -> {
      val url = args.getOrNull(1) ?: "http://localhost:8080/ws"
      shellContext(WsClient) {
        createWsClient(url)
      }
    }

    "stop" -> shellContextRemove(WsClient)

    "message" -> {
      val msg = args.getOrNull(1) ?: "Hello from WsClient"
      shellContext(WsClient, ::createWsClient).enqueueMessage(StringMessage(msg))
    }
  }
}



