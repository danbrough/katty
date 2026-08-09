package io.github.danbrough.katty

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class BashContext {
  var currentDir: Path = SystemFileSystem.resolve(Path("."))
}