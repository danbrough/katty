package io.github.danbrough.katty// src/commonMain/kotlin/CborFrameCodec.kt
import io.github.danbrough.katty.compression.BinaryMessageCodec

val CborMessageCodec = BinaryMessageCodec(cbor)