package io.github.danbrough.katty.server

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.KattyError
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.ShellContext
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.shellContext
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.serverConfig
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

private val log = logServer

open class KattyServer(
  val host: String = "127.0.0.1",
  val port: Int = DEFAULT_WEBSERVER_PORT,
  val engineFactory: ApplicationEngineFactory<*, *> = CIO,
  private val engineConfiguration: ApplicationEngine.Configuration.() -> Unit = {},
  private val applicationConfiguration: suspend Application.(KattyServer) -> Unit = {}
) : KattyContext {
  companion object : CoroutineContext.Key<KattyServer> {
    const val DEFAULT_WEBSOCKET_PATH = "/ws"
    const val DEFAULT_WEBSERVER_PORT = 8080
  }

  override val key: CoroutineContext.Key<*> = KattyServer

  lateinit var engine: EmbeddedServer<*, *>

  protected open fun configureEngine(configuration: ApplicationEngine.Configuration): Unit =
    configuration.run {
      if (this is CIOApplicationEngine.Configuration) {
        reuseAddress = true
      }

      connector {
        host = this@KattyServer.host
        port = this@KattyServer.port
      }

      engineConfiguration()
    }

  protected open suspend fun configureApplication(application: Application): Unit =
    application.applicationConfiguration(this)

  suspend fun start() {
    val context = shellContext()
    engine = embeddedServer(
      factory = engineFactory,
      rootConfig = serverConfig {
        parentCoroutineContext = context
        module {
          configureApplication(this)
        }
      },
      configure = {
        this@KattyServer.configureEngine(this)
      }
    )

    engine.startSuspend()
  }


  override fun close() {
    log.info { "ServerContext::close() .. stopping engine .." }
    if (::engine.isInitialized)
      engine.stop()
  }

}

fun commandServer(creator: suspend () -> KattyServer) =
  basicCommand("server", "server [start | stop]") {
    val action = it.getOrNull(1) ?: KattyError.errorInvalidCommandUsage("No action supplied")
    when (action) {
      "start" -> {
        shellContext(KattyServer) {
          info("starting katty webserver..")
          creator().apply {
            start()
          }
        }
      }

      "stop" -> {
        info("stopping server: shellContext:${shellContext()} terminal:${currentCoroutineContext()[KattyShell]}")
        ShellContext.remove(KattyServer)
      }
    }
  }


suspend fun Application.kattyWebSocket(
  path: String = KattyServer.DEFAULT_WEBSOCKET_PATH,
  shell: KattyShell
) {

  install(WebSockets) {

  }
}

