package io.github.danbrough.katty

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
enum class Action {
  LOGIN,
  LOGIN_ACK,
  MESSAGE
}

@Serializable
sealed class WsMessage {
  abstract val id: Uuid
  abstract val payload: String
}


@Serializable
data class ActionMessage(
  val action: Action,
  override val payload: String,
  override val id: Uuid = Uuid.generateV7()
) : WsMessage()


