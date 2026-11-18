package io.github.danbrough.katty.messages

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import kotlin.reflect.KClass


@Serializable
abstract class KattyMessage


@SerialName("RESULT")
@Serializable
data class ResultMessage(val success: Boolean, val message: String? = null) : KattyMessage()

val SuccessMessage = ResultMessage(true)

class MessageCodec(val encode: (KattyMessage) -> ByteArray, val decode: (ByteArray) -> KattyMessage)

interface KattyPayload

@Serializable
@SerialName("DATA")
data class Data(
  @Polymorphic val payload: KattyPayload, @EncodeDefault(EncodeDefault.Mode.ALWAYS) val id: Int = 2
) : KattyMessage() {
  inline fun <reified T : KattyPayload> payloadOrNull(): T? = payload as? T

  inline fun <reified T : KattyPayload> requirePayload(): T = payload as? T
    ?: error("Expected payload of type ${T::class.simpleName}, but got ${payload::class.simpleName}")
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : KattyMessage> kattySerializersModule(messages: List<KClass<out T>> = emptyList()) =
  SerializersModule {
    polymorphic(KattyMessage::class) {
      subclass(ResultMessage::class)
      subclass(Data::class)
      for (message in messages) {
        @Suppress("UNCHECKED_CAST")
        subclass(message as KClass<KattyMessage>, message.serializer())
      }
    }
    polymorphic(KattyPayload::class) {}
  }

@OptIn(InternalSerializationApi::class)
inline fun <reified T : KattyMessage> SerializersModule.withKattyMessages(vararg messages: KClass<out T>): SerializersModule =
  this + SerializersModule {
    polymorphic(KattyMessage::class) {
      messages.forEach {
        @Suppress("UNCHECKED_CAST")
        subclass(it as KClass<KattyMessage>, it.serializer())
      }
    }
  }

@OptIn(InternalSerializationApi::class)
inline fun <reified P : KattyPayload> SerializersModule.withKattyPayloads(vararg payloads: KClass<out P>): SerializersModule =
  this + SerializersModule {
    polymorphic(KattyPayload::class) {
      payloads.forEach {
        @Suppress("UNCHECKED_CAST")
        subclass(it as KClass<KattyPayload>, it.serializer())
      }
    }
  }


fun KattyMessage.asJson(stringFormat: StringFormat): String =
  stringFormat.encodeToString(PolymorphicSerializer(KattyMessage::class), this)

fun KattyMessage.asBinary(binaryFormat: BinaryFormat): ByteArray =
  binaryFormat.encodeToByteArray(PolymorphicSerializer(KattyMessage::class), this)

