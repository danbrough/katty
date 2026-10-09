package io.github.danbrough.katty.config

import io.github.danbrough.katty.BasicCommandHandler

fun BasicCommandHandler.registerConfigCommands() {
  registerCommands(
    "configDemo" to DemoConfigCommand,
    "configArgsDemo" to DemoConfigArgs,
    "configParseDemo" to DemoParseValueCommand,
  )
}
