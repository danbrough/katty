package io.github.danbrough.katty

import io.github.danbrough.katty.messages.KattyMessage
import io.github.danbrough.katty.messages.kattySerializersModule
import io.github.danbrough.katty.messages.withKattyMessages
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json


@SerialName("StringMessage")
@Serializable
data class StringMessage(val msg: String) : KattyMessage()




fun demoSerializersModule() = kattySerializersModule<KattyMessage>().withKattyMessages(StringMessage::class)

val cbor = Cbor {
  serializersModule = demoSerializersModule()
}

val json = Json {
  serializersModule = demoSerializersModule()
}
