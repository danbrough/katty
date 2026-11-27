package io.github.danbrough.katty.demos

import com.github.ajalt.mordant.widgets.HorizontalRule
import io.github.danbrough.katty.CborMessageCodec
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyTerminal
import io.github.danbrough.katty.StringMessage
import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.cbor
import io.github.danbrough.katty.json
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.messages.KattyMessage
import io.github.danbrough.katty.messages.SuccessMessage
import io.github.danbrough.katty.messages.asBinary
import io.github.danbrough.katty.messages.asJson
import kotlinx.serialization.decodeFromByteArray

private val log = logDemo

val SerializeDemoCommand = basicCommand("serialize", "serialization demo") {
  serializeDemo(input.drop(1))
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
  val bData = CborMessageCodec.encode<KattyMessage>(stringMessage)
  info("stringMessage: $stringMessage bData.size = ${bData.size}")
  val stringMessage2 = CborMessageCodec.decode<KattyMessage>(bData)
  info("stringMessage2: $stringMessage2")
  println(HorizontalRule())
}


fun KattyShell.testOld() {
  val m = SuccessMessage.asJson(json)
  info("OK:$m  m.size = ${m.length}")
  val ok = json.decodeFromString<KattyMessage>(m)
  success("got ok: $ok")
  println(HorizontalRule())
  val data = SuccessMessage.asBinary(cbor)
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