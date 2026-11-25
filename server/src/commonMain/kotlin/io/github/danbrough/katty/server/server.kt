package io.github.danbrough.katty.server

import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.command.BasicCommand
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextRegisterProvider
import io.github.danbrough.katty.shellContextRemove


private val log = logServer


fun serverCommand(cmdName: String = "server", creator: suspend () -> KattyServer) =
  cmdName to object : BasicCommand("$cmdName [start|stop]") {

    override suspend fun initialize() {
      shellContextRegisterProvider(KattyServer, creator)
    }

    override suspend fun invoke(shell: KattyShell, a: List<String>) {
      val action = a.getOrNull(1)
      log.trace { "server action: $action" }
      when (action) {
        "start" -> shellContext(KattyServer)
        "stop" -> shellContextRemove(KattyServer)
      }
    }
  }


