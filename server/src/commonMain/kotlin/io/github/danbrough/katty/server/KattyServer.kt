package io.github.danbrough.katty.server

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyShellKey
import io.github.danbrough.katty.ShellContext
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.shellContext
import io.ktor.serialization.kotlinx.json.json
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
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

private val log = logServer

open class KattyServer(
  val parentContext: CoroutineContext,
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
    application.run {

      install(IgnoreTrailingSlash)

      install(ForwardedHeaders)
      /*  install(XForwardedHeaders) {
          // Use the first proxy IP in the X-Forwarded-For chain
          useFirstProxy()
        }*/
      install(ContentNegotiation) {
        json()
      }


      install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
      }

      wsShell()
      applicationConfiguration(this@KattyServer)
    }

  suspend fun start() {
    engine = embeddedServer(
      factory = engineFactory,
      rootConfig = serverConfig {
        parentCoroutineContext = this@KattyServer.parentContext
        module {
          configureApplication(this)
        }
      },
      configure = {
        this@KattyServer.configureEngine(this)
      }
    )

    engine.startSuspend(wait = false)
  }


  override fun close() {
    log.info { "ServerContext::close() .. stopping engine .." }
    if (::engine.isInitialized)
      engine.stop()
  }

}

fun commandServer(creator: suspend KattyShell.(args: List<String>) -> KattyServer) =
  basicCommand("server", "server [start | stop]") {
    val args = it.drop(1)
    if (args.contains("start")) {
      shellContext(KattyServer) {
        info("starting katty webserver..")
        creator(args).apply {
          start()
        }
      }
    } else if (args.contains("stop")) {
      info("stopping server: shellContext:${shellContext()} terminal:${currentCoroutineContext()[KattyShellKey]}")
      ShellContext.remove(KattyServer)
    } else {
      warn("unknown command: $args")
    }
  }



