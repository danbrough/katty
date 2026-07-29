package io.github.danbrough.katty

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun demoMain(args: Array<String>) {

/*
  listOf("/usr","/bin","/test.txt").map{Path(it)}.forEach { p->
    var md = SystemFileSystem.metadataOrNull(p)
    println("metadata for $p isDir: ${md?.isDirectory} isRegularFile: ${md?.isRegularFile}")
    val pp = SystemFileSystem.resolve(p)
    md = SystemFileSystem.metadataOrNull(pp)
    println("metadata for $pp isDir: ${md?.isDirectory} isRegularFile: ${md?.isRegularFile}")

  }
*/

  val configDir = Path(KattyUtils.getEnv("HOME")!!, ".katty")
  println("configDir: $configDir")
  if (!SystemFileSystem.exists(configDir)) {
    println("Creating configuration dir at $configDir..")
    SystemFileSystem.createDirectories(configDir, true)
  }
  val terminal = KTerminal(Path(configDir, "history.txt"))
  terminal.commandHandlers.add(LsCommandHandler)
  terminal.run()
}