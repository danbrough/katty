package io.github.danbrough.katty.client

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.messages.KattyMessage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
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
import org.danbrough.klog.logger
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds


internal val logClient = logger("KATTY_CLIENT")
private val log = logClient

class WsClient(
  val scope: CoroutineScope,
  val url: String,
  override val key: CoroutineContext.Key<*> = WsClient,
  config: HttpClientConfig<HttpClientEngineConfig>.() -> Unit = {},
  val clientEngine: HttpClientEngineFactory<*> = CIO,
  val requestBuilder: HttpRequestBuilder.() -> Unit = {},
  val sendMessage: suspend DefaultClientWebSocketSession.(KattyMessage) -> Unit,
  val sessionHandler: suspend DefaultClientWebSocketSession.(WsClient) -> Unit
) : KattyContext {
  companion object : CoroutineContext.Key<WsClient>

  private val client = HttpClient(clientEngine) {
    install(WebSockets) {
      pingIntervalMillis = 15_000 // Send app-level ping frames every 15s
    }
    config()
  }

  private var session: DefaultClientWebSocketSession? = null

  private class PendingMessage(
    val message: KattyMessage,
    val responseDeferred: CompletableDeferred<KattyMessage> = CompletableDeferred()
  )

  // 1. Create a backed buffer (queue) for outgoing messages
  private val outgoingQueue = Channel<PendingMessage>(
    capacity = 10,
    onBufferOverflow = BufferOverflow.SUSPEND
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
    outgoingQueue.consumeAsFlow().collect { pending ->
      try {
        log.trace { "WsClient::processOutgoingQueue() sending ${pending.message} session: $session" }
        var clientSession = session
        while (clientSession == null) {
          delay(1.seconds)
          clientSession = session
        }
        clientSession.sendMessage(pending.message)
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
