package io.github.danbrough.katty.config

import io.github.danbrough.katty.BasicCommand
import io.github.danbrough.katty.BasicCommandHandler
import io.github.danbrough.katty.KTerminal

fun BasicCommandHandler.registerConfigCommands() {
  registerCommands(
    "configDemo" to DemoConfigCommand,
    "configArgsDemo" to DemoConfigArgs,
    "configParseDemo" to DemoParseValueCommand,
    "configSerializeTest" to BasicCommand("Serialization test", KTerminal::serializeTest)
  )
}
