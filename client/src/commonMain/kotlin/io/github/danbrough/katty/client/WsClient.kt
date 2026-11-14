package io.github.danbrough.katty.client

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.messages.KattyMessage
import io.github.danbrough.katty.messages.asBinary
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.cbor.Cbor
import org.danbrough.klog.logger
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds


internal val logClient = logger("KATTY_CLIENT")
private val log = logClient

class WsClient(
  val scope: CoroutineScope,
  val url: String,
  override val key: CoroutineContext.Key<*> = WsClient,
  val cbor: Cbor,
  config: HttpClientConfig<*>.() -> Unit = {},
  val requestBuilder: HttpRequestBuilder.() -> Unit = {},
  val sessionHandler: suspend DefaultClientWebSocketSession.(WsClient) -> Unit
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
  private val outgoingQueue = Channel<KattyMessage>(
    capacity = 10, //Channel.BUFFERED,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  private var job: Job? = null


  private fun connect() {
    log.error { "WsClient::connect" }
    // Prevent duplicate connection attempts
    if (job?.isActive == true) return

    job = scope.launch {
      // 2. Concurrently run the outgoing queue worker alongside incoming processing
      launch {
        processOutgoingQueue()
      }

      val wsUrl = url.replaceFirst("https", "wss").replaceFirst("http", "ws")
      log.info { "WsClient:: connecting to websocket session at $wsUrl.." }

      while (isActive) {
        try {
          // Establishes a persistent, reusable WebSockets stream
          client.webSocket(wsUrl, requestBuilder) {
            session = this
            sessionHandler(this@WsClient)
          }
        } catch (e: Throwable) {
          println("WebSocket Error: ${e.message}")
          if (isActive) {
            delay(5.seconds)
            log.debug { "WsClient::reconnecting .." }
            session = null
            continue
          }
        } finally {
          session = null
          log.warn { "WsClient:: WebSocket Disconnected" }
        }
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
        session!!.send(Frame.Binary(true, message.asBinary(cbor)))
      } catch (e: Exception) {
        log.error { "Failed to send message: $e" }
        // Optional: Re-enqueue the message if delivery fails
        outgoingQueue.send(message)
      }
    }
  }

  suspend fun enqueueMessage(message: KattyMessage) {
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
