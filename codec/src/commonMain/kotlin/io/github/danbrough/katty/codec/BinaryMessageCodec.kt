package io.github.danbrough.katty.codec// src/commonMain/kotlin/CborFrameCodec.kt
import io.github.danbrough.katty.messages.KattyMessage
import io.github.danbrough.katty.messages.MessageCodec
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.Deflate
import korlibs.io.compression.uncompress
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

class BinaryMessageCodec(val cbor: BinaryFormat) : MessageCodec {


  companion object {

    const val FLAG_RAW: Byte = 0x00
    const val FLAG_DEFLATE: Byte = 0x01
  }


  /**
   * Serializes [value] to CBOR and prepends a 1-byte compression flag.
   * Compresses only when the CBOR payload meets [minCompressBytes] AND actually shrinks.
   */
  inline fun <reified T> encode(
    value: T, minCompressBytes: Int = 256
  ): ByteArray {
    val rawCbor = cbor.encodeToByteArray<T>(value)

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
  inline fun <reified T> decode(packet: ByteArray): T {
    require(packet.isNotEmpty()) { "Empty binary frame received" }

    val flag = packet[0]
    val payload = packet.copyOfRange(1, packet.size)

    val cborBytes = when (flag) {
      FLAG_RAW -> payload
      FLAG_DEFLATE -> payload.uncompress(Deflate)
      else -> throw IllegalArgumentException("Unknown compression flag: $flag")
    }

    return cbor.decodeFromByteArray(cborBytes)
  }

  override fun encode(message: KattyMessage): ByteArray = encode<KattyMessage>(message)
  override fun decode(data: ByteArray): KattyMessage = decode<KattyMessage>(data)
}



