package io.github.danbrough.katty.client

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.WsMessage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import kotlin.coroutines.CoroutineContext

private val log = logClient

class WsClient(
  val scope: CoroutineScope,
  val url: String,
  override val key: CoroutineContext.Key<*> = WsClient,
  config: HttpClientConfig<*>.() -> Unit = {},
  val requestBuilder: HttpRequestBuilder.() -> Unit = {}
) : KattyContext {
  companion object : CoroutineContext.Key<WsClient>

  private val client = HttpClient {

    install(WebSockets) {
      pingIntervalMillis = 15_000 // Send app-level ping frames every 15s
    }

    config()
  }

  private var session: DefaultClientWebSocketSession? = null

  // 1. Create a backed buffer (queue) for outgoing messages
  // Using an explicit capacity prevents unbounded memory growth if the connection drops
  private val outgoingQueue = Channel<WsMessage>(
    capacity = 10, //Channel.BUFFERED,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  private var job: Job? = null


  private fun connect() {
    log.error { "WsClient::connect" }
    // Prevent duplicate connection attempts
    if (job?.isActive == true) return

    job = scope.launch {
      try {
        // Establishes a persistent, reusable WebSockets stream
        val wsUrl = url.replaceFirst("https", "wss").replaceFirst("http", "ws")
        log.warn { "WsClient:: connecting to websocket session at $wsUrl.." }

        session = client.webSocketSession(wsUrl) {
          //TODO basicAuth(username = Mv4kConfig.adminUser, password = Mv4kConfig.adminPassword)
          requestBuilder()
        }

        client.webSocket(wsUrl) {
          session = this

          // 2. Concurrently run the outgoing queue worker alongside incoming processing
          launch(KattyUtils.ioDispatcher) {
            processOutgoingQueue()
          }

          log.trace { "launched processOutgoingQueue()" }

          // 3. Process incoming messages (keeps the block alive)
          for (frame in incoming) {
            log.trace { "Received: $frame" }
            if (frame is Frame.Text) {
              println("Received: ${frame.readText()}")
            }
          }
        }
      } catch (e: Exception) {
        println("WebSocket Error: ${e.message}")
      } finally {
        session = null
        log.warn { "WsClient:: WebSocket Disconnected" }
      }
    }
  }


  // 4. The underlying Queue Worker
  private suspend fun processOutgoingQueue() {
    // Consuming as a flow ensures safe, reactive extraction
    outgoingQueue.consumeAsFlow().collect { message ->
      try {
        // If session is null (disconnected), this loop pauses or drops based on implementation.
        // Here, we wait for a valid session before delivering, or you can handle disconnection strategies.
        log.trace { "WsClient::processOutgoingQueue() sending $message session: $session" }

        session?.send(Frame.Binary(false, Cbor.encodeToByteArray<WsMessage>(message)))
      } catch (e: Exception) {
        log.error { "Failed to send message: $e" }
        // Optional: Re-enqueue the message if delivery fails
        // outgoingQueue.send(message)
      }
    }
  }

  // 5. The Public Api to add things to the queue safely from any thread/coroutine
  suspend fun enqueueMessage(message: WsMessage) {
    outgoingQueue.send(message)
  }


  override fun close() {
    log.info { "WsClient::close()" }
    job?.cancel()
    session = null
  }

  init {
    connect()
  }
}
