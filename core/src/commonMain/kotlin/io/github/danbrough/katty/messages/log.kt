package io.github.danbrough.katty.messages

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


@Serializable
abstract class KattyMessage

@Serializable
@SerialName("OK")
data object OK : KattyMessage()

interface KattyPayload

/*
@Serializable
@SerialName("Thang")
data class Thang(val name: String) : KattyPayload*/

@Serializable
@SerialName("DATA")
data class Data(
  @Polymorphic val payload: KattyPayload, val id: Int = 2
) : KattyMessage() {
  inline fun <reified T : KattyPayload> payloadOrNull(): T? = payload as? T

  inline fun <reified T : KattyPayload> requirePayload(): T = payload as? T
    ?: error("Expected payload of type ${T::class.simpleName}, but got ${payload::class.simpleName}")
}

val kattySerializersModule = SerializersModule {
  polymorphic(KattyMessage::class) {
    subclass(OK::class)
    subclass(Data::class)
  }/*  polymorphic(KattyPayload::class) {
      subclass(Thang::class)
    }*/
}

fun KattyMessage.toJson(stringFormat: StringFormat): String =
  stringFormat.encodeToString(PolymorphicSerializer(KattyMessage::class), this)

fun KattyMessage.toCbor(binaryFormat: BinaryFormat): ByteArray =
  binaryFormat.encodeToByteArray(PolymorphicSerializer(KattyMessage::class), this)

