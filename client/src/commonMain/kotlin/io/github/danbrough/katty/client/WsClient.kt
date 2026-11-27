package io.github.danbrough.katty.client

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.messages.KattyMessage
import io.github.danbrough.katty.messages.MessageCodec
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.websocket.Frame
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


private val log = logClient

val httpClient: HttpClient by lazy {
  HttpClient(ktorClientFactory()) {
    install(WebSockets) {
      pingIntervalMillis = 15_000 // Send app-level ping frames every 15s
    }

    clientEngineConfig()
  }
}

class WsClient(
  val scope: CoroutineScope,
  val url: String,
  val codec: MessageCodec,
  override val key: CoroutineContext.Key<*> = WsClient,
  val requestBuilder: HttpRequestBuilder.() -> Unit = {},
  val sessionHandler: suspend DefaultClientWebSocketSession.(WsClient) -> Unit
) : KattyContext {
  companion object : CoroutineContext.Key<WsClient>


  private var session: DefaultClientWebSocketSession? = null

  private class PendingMessage(
    val message: KattyMessage,
    val responseDeferred: CompletableDeferred<KattyMessage> = CompletableDeferred()
  )

  // 1. Create a backed buffer (queue) for outgoing messages
  private val outgoingQueue = Channel<PendingMessage>(
    capacity = 10, onBufferOverflow = BufferOverflow.SUSPEND
  )

  private val pendingResponsesMutex = Mutex()
  private val pendingResponses = mutableListOf<PendingMessage>()

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
          httpClient.webSocket(wsUrl, requestBuilder) {
            session = this
            sessionHandler(this@WsClient)
          }
        } catch (e: Throwable) {
          println("WebSocket Error: ${e.message} - ${e.stackTraceToString()}")
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
    outgoingQueue.consumeAsFlow().collect { pending ->
      try {
        log.trace { "WsClient::processOutgoingQueue() sending ${pending.message} session: $session" }
        var clientSession = session
        var delayCount = 0
        while (clientSession == null) {
          delay(kotlin.math.ln(delayCount.toFloat()).toInt().milliseconds)
          delayCount++
          clientSession = session
        }

        clientSession.send(Frame.Binary(fin = true, codec.encode(pending.message)))

        //session!!.send(Frame.Binary(true, pending.message.asBinary(cbor)))
      } catch (e: Exception) {
        log.error { "Failed to send message: $e" }
        pendingResponsesMutex.withLock {
          pendingResponses.remove(pending)
        }
        pending.responseDeferred.completeExceptionally(e)
      }
    }
  }

  suspend fun enqueueMessage(message: KattyMessage): KattyMessage {
    val pending = PendingMessage(message)
    pendingResponsesMutex.withLock {
      pendingResponses.add(pending)
    }
    outgoingQueue.send(pending)
    return try {
      pending.responseDeferred.await()
    } finally {
      pendingResponsesMutex.withLock {
        pendingResponses.remove(pending)
      }
    }
  }

  suspend fun onResponseReceived(response: KattyMessage) {
    val pending = pendingResponsesMutex.withLock {
      if (pendingResponses.isNotEmpty()) pendingResponses.removeAt(0) else null
    }
    pending?.responseDeferred?.complete(response)
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
