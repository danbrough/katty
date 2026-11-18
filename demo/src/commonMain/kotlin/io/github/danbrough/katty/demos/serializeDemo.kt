package io.github.danbrough.katty.demos

import com.github.ajalt.mordant.widgets.HorizontalRule
import io.github.danbrough.katty.CborFrameCodec
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyTerminal
import io.github.danbrough.katty.StringMessage
import io.github.danbrough.katty.cbor
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.json
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.messages.Data
import io.github.danbrough.katty.messages.KattyMessage
import io.github.danbrough.katty.messages.KattyPayload
import io.github.danbrough.katty.messages.OK
import io.github.danbrough.katty.messages.asBinary
import io.github.danbrough.katty.messages.asJson
import io.github.danbrough.katty.messages.kattySerializersModule
import io.github.danbrough.katty.messages.withKattyPayloads
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.json.Json

private val log = logDemo

val SerializeDemoCommand = basicCommand("serialize", "serialization demo") {
  serializeDemo(it.drop(1))
}


fun KattyShell.test() {

  val data = StringMessage("Hello").asJson(json)
  info("data: $data size:${data.length}")
  val message = json.decodeFromString<KattyMessage>(data)
  info("message: $message")
  (this as KattyTerminal).println(HorizontalRule())

  val cData = StringMessage("Hello").asBinary(cbor)
  info("DATA:cData.size = ${cData.size}")
  val message2 = cbor.decodeFromByteArray<KattyMessage>(cData)
  info("message2: $message2")
  println(HorizontalRule())
  val stringMessage = StringMessage("This is my string message")
  val bData = CborFrameCodec.encode<KattyMessage>(stringMessage)
  info("stringMessage: $stringMessage bData.size = ${bData.size}")
  val stringMessage2 = CborFrameCodec.decode<KattyMessage>(bData)
  info("stringMessage2: $stringMessage2")
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
  println(HorizontalRule())
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


  }.exceptionOrNull()?.also {
    error("finished with error: ${it.message}")
  }
}