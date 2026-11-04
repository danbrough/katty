package io.github.danbrough.katty

import kotlinx.serialization.Serializable

@Serializable
data class WsMessage(
  val action: String,
  val payload: String,
  val timestamp: Long
)