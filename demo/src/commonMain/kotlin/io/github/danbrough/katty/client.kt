package io.github.danbrough.katty

import io.github.danbrough.katty.client.WsClient
import io.github.danbrough.katty.client.commandClient

fun demoClientCommand() =
  commandClient { args ->

    info(
      """
      starting wsclient args: $args 
     
  """.trimIndent()
    )


    WsClient(shellScope, "http://localhost:8080/ws")
  }


