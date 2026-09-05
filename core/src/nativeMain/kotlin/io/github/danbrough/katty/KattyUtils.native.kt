@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.danbrough.katty

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered
import platform.posix.FILE
import platform.posix.fread
import platform.posix.getenv
import platform.posix.pclose
import platform.posix.popen

@OptIn(ExperimentalForeignApi::class)
private class PopenSource(private val fp: CPointer<FILE>) : RawSource {
  override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
    val buffer = ByteArray(byteCount.toInt().coerceAtMost(8192))
    val read = buffer.usePinned { pinned ->
      fread(pinned.addressOf(0), 1.convert(), buffer.size.convert(), fp).toLong()
    }
    if (read <= 0L) return -1L
    sink.write(buffer, 0, read.toInt())
    return read
  }

  override fun close() {
    pclose(fp)
  }
}

actual object KattyUtils  {
  @OptIn(ExperimentalForeignApi::class)
  actual  fun getEnv(name: String): String? = getenv(name)?.toKString()

  @OptIn(ExperimentalForeignApi::class)
  actual fun exec(command: List<String>): Source {
    val cmdString = command.joinToString(" ")
    val fp = popen(cmdString, "r") ?: throw IOException("Failed to execute command: $cmdString")
    return PopenSource(fp).buffered()
  }
}
