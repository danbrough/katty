package io.github.danbrough.katty.demos

import com.github.ajalt.mordant.widgets.HorizontalRule
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyTerminal
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.messages.Data
import io.github.danbrough.katty.messages.KattyMessage
import io.github.danbrough.katty.messages.KattyPayload
import io.github.danbrough.katty.messages.OK
import io.github.danbrough.katty.messages.asBinary
import io.github.danbrough.katty.messages.asJson
import io.github.danbrough.katty.messages.kattySerializersModule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.json.Json

private val log = logDemo

val SerializeDemoCommand = basicCommand("serialize", "serialization demo") {
  serializeDemo(it.drop(1))
}


/*@Serializable
abstract class KattyMessage

@Serializable
@SerialName("OK")
data object OK : KattyMessage()


interface KattyPayload*/

@Serializable
@SerialName("THANG")
data class Thang(val name: String) : KattyPayload

/*@Serializable
@SerialName("DATA")
data class Data(
  @Polymorphic val payload: KattyPayload,
  val id: Int = 2
) : KattyMessage() {
  inline fun <reified T : KattyPayload> payloadOrNull(): T? = payload as? T

  inline fun <reified T : KattyPayload> requirePayload(): T =
    payload as? T
      ?: error("Expected payload of type ${T::class.simpleName}, but got ${payload::class.simpleName}")
}*/

/*val kattySerializersModule = SerializersModule {
  polymorphic(KattyMessage::class) {
    subclass(OK::class)
    subclass(Data::class)
  }
  polymorphic(KattyPayload::class) {
    subclass(Thang::class)
  }
}*/

val kattySerializersModule = kattySerializersModule(Thang::class)
val cbor = Cbor {
  serializersModule = kattySerializersModule
}

val json = Json {
  serializersModule = kattySerializersModule
}


fun KattyShell.test() {

  val data = Data(Thang("Hello")).asJson(json)
  info("data: $data size:${data.length}")
  val message = json.decodeFromString<KattyMessage>(data)
  info("message: $message")
  (this as KattyTerminal).println(HorizontalRule())

  val cData = Data(Thang("Hello")).asBinary(cbor)
  info("DATA:cData.size = ${cData.size}")
  val message2 = cbor.decodeFromByteArray<KattyMessage>(cData)
  info("message2: $message2")
  println(HorizontalRule())

}


fun KattyShell.testOld() {
  val m = OK.asJson(json)
  info("OK:$m  m.size = ${m.length}")
  val ok = json.decodeFromString<KattyMessage>(m)
  success("got ok: $ok")
  println(HorizontalRule())
  val data = OK.asBinary(cbor)
  info("OK:$m  data.size = ${data.size}")
}


private suspend fun KattyShell.serializeDemo(args: List<String>) {
  info("serializeDemo: $args")

  runCatching {

    test()
    /*    val m = cbor.encodeToByteArray(PolymorphicSerializer(MyMessage::class), MyMessage.OK)
        info("OK: m.size = ${m.size}")
        val ok = cbor.decodeFromByteArray<MyMessage>(m)
        success("got ok: $ok")
        var data = json.encodeToString(MyMessage.OK)
        info("OK $data length:${data.length}")

        data = json.encodeToString(
          MyMessage.DataMessage(Thang(123, "Hello"))
        )
        info("$data length:${data.length}")*/


  }.exceptionOrNull().also {
    warn("finished message: ${it?.message}")
  }
}