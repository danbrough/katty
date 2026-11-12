package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.logDemo
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.uuid.Uuid


private val log = logDemo

val SerializeDemoCommand = basicCommand("serialize", "serialization demo") {
  serializeDemo(it.drop(1))
}

@Serializable
sealed interface KattyMessage {
  @Serializable
  object OK : KattyMessage
}

@Serializable
data class DataMessage<out T>(val data: T, val id: Uuid = Uuid.generateV7()) : KattyMessage


@Serializable
data class Thang(val id: Long, val name: String)


val kattySerializersModule = SerializersModule {
  polymorphic(KattyMessage::class) {
    subclass(KattyMessage.OK::class)
    subclass(DataMessage.serializer(Thang.serializer()))
  }
}

val cbor = Cbor {
  serializersModule = kattySerializersModule
}

val json = Json {
  serializersModule = kattySerializersModule
}

private suspend fun KattyShell.serializeDemo(args: List<String>) {
  info("serializeDemo: $args")

  runCatching {

    val m = cbor.encodeToByteArray(PolymorphicSerializer(KattyMessage::class), KattyMessage.OK)
    info("m.size = ${m.size}")
    var data = json.encodeToString(PolymorphicSerializer(KattyMessage::class), KattyMessage.OK)
    info("$data length:${data.length}")

    data = json.encodeToString(PolymorphicSerializer(KattyMessage::class), DataMessage(Thang(123, "Hello")))
    info("$data length:${data.length}")


  }.exceptionOrNull().also {
    warn("finished message: ${it?.message}")
  }
}