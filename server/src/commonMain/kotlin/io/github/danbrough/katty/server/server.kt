package io.github.danbrough.katty.server

import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyShellKey
import io.github.danbrough.katty.command.BaseCommand
import io.github.danbrough.katty.isSingleRun
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextRemove
import kotlinx.coroutines.currentCoroutineContext


private val log = logServer

fun serverCommand() = "server" to object : BaseCommand<List<String>, Unit>() {

  override val helpText: String = "server [start|stop]"

  override suspend fun invoke(shell: KattyShell, a: List<String>): Unit = shell.run {
    val args = a.drop(1)
    info("server command: $args singleRun: ${isSingleRun()}")
    if (args.contains("start")) {
      shellContext(KattyServer)
    } else if (args.contains("stop")) {
      info("stopping server: shellContext:${shellContext()} shell:${currentCoroutineContext()[KattyShellKey]}")
      shellContextRemove(KattyServer)
    } else {
      warn("invalid server command: $args")
    }
  }
}
