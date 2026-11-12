package io.github.danbrough.katty.client

import io.github.danbrough.katty.Action
import io.github.danbrough.katty.ActionMessage
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyShellKey
import io.github.danbrough.katty.WsMessage
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextRemove
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.danbrough.klog.logger

internal val logClient = logger("KATTY_CLIENT")
private val log = logClient
private const val USAGE = "clientOld [start | stop | message (content)]"
fun commandClientOld(creator: KattyShell.(args: List<String>) -> KattyClient) =
  basicCommand("clientOld", USAGE) {
    val args = it.drop(1)
    when (val cmd = args.getOrNull(0)) {
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

      "test" -> {
        val message: WsMessage = ActionMessage(Action.MESSAGE,"This is my message")
        val bytes = Cbor.encodeToByteArray(message)
        log.info { "message: ${Cbor.decodeFromByteArray<WsMessage>(bytes)}" }
      }

      "message" -> {
        val message = args.getOrNull(1) ?: run {
          info(USAGE)
          return@basicCommand
        }
        info("sending message: $message...")
        shellContext(KattyClient) {
          info("starting katty client..")
          creator(args)
        }.sendCborMessage(ActionMessage(Action.MESSAGE,message) as WsMessage)
      }

      else -> {
        if (cmd == null) info(USAGE)
        else warn("unhandled cmd: $cmd")
      }
    }
  }


