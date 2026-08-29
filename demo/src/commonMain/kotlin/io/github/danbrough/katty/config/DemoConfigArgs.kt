package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import io.github.danbrough.katty.BasicCommand
import io.github.danbrough.katty.GlobalConfig
import io.github.danbrough.katty.KTerminal
import io.github.danbrough.katty.ServerConfig
import kotlinx.io.files.Path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

val DemoConfigArgs = BasicCommand("demo configuration args") {
  demoConfigArgs(it)
}



suspend fun KTerminal.demoConfigArgs(args: List<String>) {

  println("demoConfigArgs: ${args.joinToString(",")}")

  val configText = Path("./demo/src/commonMain/resources/config.toml").readText()
  val configToml = TomlParser(TomlInputConfig()).parseString(configText)

  terminal.printTomlFile("Config toml: ", configToml)
  terminal.printSectionTitle("File Comments", configToml.comments.joinToString("\n"))



  buildString {
    configToml.comments.forEach {
      append("comment: $it")
    }
    configToml.children.flatMap { it.children }.forEach { child ->

      if (child.comments.isNotEmpty()) {
        child.inlineComment.takeIf { it.isNotBlank() }?.also { comment ->
          append("inline: [$comment]\n")
        }
        child.comments.forEach { comment ->
          append("comment: [$comment]\n")
        }
        append("node: ${child.name}\n")
      }
    }
  }.also {
    terminal.printSectionTitle("Server Comments", it)
  }

  val config: GlobalConfig = Toml.decodeFromString(configToml.tomlString())
  terminal.printSectionTitle("Server Config", config.toString())
  terminal.printSectionTitle("Server Config Toml", Toml.encodeToString(config))


  args.forEach { arg ->
    println(
      "arg: $arg split: ${
        arg.split("-+".toRegex()).filter { it.isNotBlank() }.joinToString(".")
      }"
    )
  }

  val serializer = ServerConfig.serializer()
  val d = serializer.descriptor
  println("Serial name: ${d.serialName}")


}