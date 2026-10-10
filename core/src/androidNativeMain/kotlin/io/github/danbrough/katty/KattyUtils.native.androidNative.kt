package io.github.danbrough.katty

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.io.files.Path
import platform.posix.stat

@OptIn(ExperimentalForeignApi::class)
actual fun getLastModifiedTimeNative(path: Path): Long {

  // memScoped handles standard C native memory allocation safely
  return memScoped {
    val fileStat = alloc<stat>()

    // Call the POSIX stat function
    val result = stat(path.toString(), fileStat.ptr)

    if (result == 0) {
      // st_mtime represents seconds since the epoch.
      // Multiply by 1000 to convert to milliseconds.

      fileStat.st_mtim.tv_sec * 1000L
    } else {
      0L // File doesn't exist or permission denied
    }
  }

}