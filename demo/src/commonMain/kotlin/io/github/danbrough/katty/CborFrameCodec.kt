package io.github.danbrough.katty// src/commonMain/kotlin/CborFrameCodec.kt
import io.github.danbrough.katty.messages.KattyMessage
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readBytes
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.Deflate
import korlibs.io.compression.uncompress
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

object CborFrameCodec {
  const val FLAG_RAW: Byte = 0x00
  const val FLAG_DEFLATE: Byte = 0x01

  /**
   * Serializes [value] to CBOR and prepends a 1-byte compression flag.
   * Compresses only when the CBOR payload meets [minCompressBytes] AND actually shrinks.
   */
  inline fun <reified T> encode(
    fmt: BinaryFormat, value: T, minCompressBytes: Int = 256
  ): ByteArray {
    val rawCbor = fmt.encodeToByteArray(value)

    if (rawCbor.size >= minCompressBytes) {
      val compressed = rawCbor.compress(Deflate)
      // Only use compressed bytes if they are strictly smaller than raw CBOR
      if (compressed.size < rawCbor.size) {
        return ByteArray(compressed.size + 1).apply {
          this[0] = FLAG_DEFLATE
          compressed.copyInto(this, destinationOffset = 1)
        }
      }
    }

    return ByteArray(rawCbor.size + 1).apply {
      this[0] = FLAG_RAW
      rawCbor.copyInto(this, destinationOffset = 1)
    }
  }

  /**
   * Reads the 1-byte header, decompresses if needed, and decodes the CBOR payload.
   */
  inline fun <reified T> decode(fmt: BinaryFormat, packet: ByteArray): T {
    require(packet.isNotEmpty()) { "Empty binary frame received" }

    val flag = packet[0]
    val payload = packet.copyOfRange(1, packet.size)

    val cborBytes = when (flag) {
      FLAG_RAW -> payload
      FLAG_DEFLATE -> payload.uncompress(Deflate)
      else -> throw IllegalArgumentException("Unknown compression flag: $flag")
    }

    return fmt.decodeFromByteArray(cborBytes)
  }
}

// Convenience extensions for both WebSocketServerSession and DefaultClientWebSocketSession
suspend inline fun <reified T> WebSocketSession.sendCborFrame(
  fmt: BinaryFormat, value: T, minCompressBytes: Int = 256
) {
  val packet = CborFrameCodec.encode(fmt, value, minCompressBytes)
  send(Frame.Binary(fin = true, data = packet))
}

suspend fun WebSocketSession.sendKattyMessage(
  fmt: BinaryFormat,
  msg: KattyMessage,
  minCompressBytes: Int = 256
) = sendCborFrame<KattyMessage>(fmt, msg, minCompressBytes)

inline fun <reified T> Frame.Binary.decodeCborFrame(fmt: BinaryFormat): T {
  return CborFrameCodec.decode(fmt, this.data)
}

fun Frame.Binary.decodeKattyMessage(fmt: BinaryFormat): KattyMessage =
  decodeCborFrame<KattyMessage>(fmt)
