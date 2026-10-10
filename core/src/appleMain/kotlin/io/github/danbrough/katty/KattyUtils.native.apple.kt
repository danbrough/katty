package io.github.danbrough.katty

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.io.files.Path
import platform.posix.stat
@OptIn(ExperimentalForeignApi::class)
actual fun getLastModifiedTimeNative(path: Path): Long {
  return memScoped {
    val fileStat = alloc<stat>()
    val result = stat(path.toString(), fileStat.ptr)
    if (result == 0) {
      fileStat.st_mtimespec.tv_sec * 1000L
    } else {
      0L
    }
  }
}