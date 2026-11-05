package io.github.danbrough.katty.client

import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyShellKey
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextRemove
import kotlinx.coroutines.currentCoroutineContext
import org.danbrough.klog.logger

internal val logClient = logger("KATTY_CLIENT")

fun commandClient(creator: KattyShell.(args: List<String>) -> KattyClient) =
  basicCommand("client", "client [start | stop | message (content)]") {
    val args = it.drop(1)
    when (args.getOrNull(0)) {
      "start" -> {
        shellContext(KattyClient) {
          info("starting katty client..")
          creator(args)
        }
      }

      "stop" -> {
        info("stopping client: shellContext:${shellContext()} shell:${currentCoroutineContext()[KattyShellKey]}")
        shellContextRemove(KattyClient)
      }

      else -> {
        warn("unhandled cmd: ${it.getOrNull(0)}")
      }
    }
  }


