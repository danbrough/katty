package io.github.danbrough.katty

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



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
)

@Serializable
data class GlobalConfig(val appName: String = "My App", val server: ServerConfig)