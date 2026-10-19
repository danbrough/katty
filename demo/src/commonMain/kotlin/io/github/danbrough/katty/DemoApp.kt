package io.github.danbrough.katty

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.coroutines.CoroutineContext

class DemoApp : CoroutineContext.Element {
  companion object : CoroutineContext.Key<DemoApp>

  override val key: CoroutineContext.Key<*> = DemoApp

}


/**
 * Server config comment
 */
@Serializable
data class ServerConfig(
  //message inline comment
  @SerialName("welcomeMessage")
  val message: String,
  //bindAddress comment
  val bindAddress: String = "127.0.0.1",
  val port: Int = 80,
  //qos comment
  val qos: Boolean = false,
)

@Serializable
data class DemoAppConfig(val appName: String = "My App", val server: ServerConfig)