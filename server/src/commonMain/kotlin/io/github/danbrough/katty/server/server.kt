package io.github.danbrough.katty.server

import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyShellKey
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.isSingleRun
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextRemove
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import org.danbrough.klog.logger

internal val logServer = logger("KATTY_SERVER")

fun commandServer(creator: KattyShell.(args: List<String>) -> KattyServer) =
  basicCommand("server", "server [start | stop]") {
    val args = it.drop(1)
    info("server command: $args singleRun: ${isSingleRun()}")
    if (args.contains("start")) {
      shellContext(KattyServer) {
        info("starting katty webserver..")
        creator(args).apply {
          shellScope.launch {
            start()
          }
        }
      }
    } else if (args.contains("stop")) {
      info("stopping server: shellContext:${shellContext()} shell:${currentCoroutineContext()[KattyShellKey]}")
      shellContextRemove(KattyServer)
    } else {
      warn("unknown command: $args")
    }
  }


