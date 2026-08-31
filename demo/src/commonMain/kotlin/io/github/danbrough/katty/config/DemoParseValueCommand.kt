package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.tree.nodes.parseValue
import io.github.danbrough.katty.BasicCommand

val DemoParseValueCommand = BasicCommand("parseValue") { args ->
  val config = TomlInputConfig(allowEmptyValues = true)
  args.drop(1).forEach {
    val value = it.parseValue(0,config)
    println("arg: [$it] value: [${value.content}] of type: ${value::class.simpleName}")
  }
}