package io.github.danbrough.katty

import io.github.danbrough.katty.client.KattyClient
import io.github.danbrough.katty.client.WsClient
import io.github.danbrough.katty.client.commandClient
import io.github.danbrough.katty.client.commandClientOld
import kotlinx.coroutines.launch

fun demoClientCommand() =
  commandClient { args ->

    info(
      """
      starting wsclient args: $args 
     
  """.trimIndent()
    )


    WsClient(shellScope, "http://localhost:8080/ws")
  }


fun demoClientCommandOld() =
  commandClientOld { args ->

    info(
      """
      starting katty client args: $args 
     
  """.trimIndent()
    )


    KattyClient().also { 
      shellScope.launch {
        it.connectAndListen()
      }
    }
  }
