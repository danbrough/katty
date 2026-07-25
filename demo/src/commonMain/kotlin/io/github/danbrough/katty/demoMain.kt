package io.github.danbrough.katty

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun demoMain(args: Array<String>) {
  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")
  println("configDir: $configDir")
  if (!SystemFileSystem.exists(configDir)) {
    println("Creating configuration dir at $configDir..")
    SystemFileSystem.createDirectories(configDir, true)
  }
  val terminal = KTerminal(Path(configDir, "history.txt"))
  terminal.run()
}