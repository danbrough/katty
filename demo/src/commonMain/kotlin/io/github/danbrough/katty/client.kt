package io.github.danbrough.katty

import io.github.danbrough.katty.client.KattyClient
import io.github.danbrough.katty.client.commandClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun demoClientCommand() = commandClient { args ->

  info(
    """
      starting katty client.. args: $args 
     
  """.trimIndent()
  )



  KattyClient().also {
    shellScope.launch {
      it.connectAndListen()
    }
  }
}