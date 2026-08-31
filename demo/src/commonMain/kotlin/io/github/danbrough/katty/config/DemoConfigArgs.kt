package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.tree.nodes.TomlArrayOfTablesElement
import com.akuleshov7.ktoml.tree.nodes.TomlFile
import com.akuleshov7.ktoml.tree.nodes.TomlInlineTable
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValueArray
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValuePrimitive
import com.akuleshov7.ktoml.tree.nodes.TomlNode
import com.akuleshov7.ktoml.tree.nodes.TomlStubEmptyNode
import com.akuleshov7.ktoml.tree.nodes.TomlTable
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlArray
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlBasicString
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlBoolean
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlDateTime
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlDouble
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlLiteralString
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlLong
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlNull
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlUnsignedLong
import io.github.danbrough.katty.BasicCommand
import io.github.danbrough.katty.DemoAppConfig
import io.github.danbrough.katty.KTerminal
import io.github.danbrough.katty.readText
import kotlinx.io.files.Path
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

  val config: DemoAppConfig = Toml.decodeFromString(configToml.tomlString())
  terminal.printSectionTitle("Server Config", config.toString())
  terminal.printSectionTitle("Server Config Toml", Toml.encodeToString(config))


  val configArgs = args.map { arg ->
    arg.split("-+".toRegex()).filter { it.isNotBlank() }.joinToString(".")
  }.map { arg ->
    //split on "=" ..
    arg.split('=').let { it[0] to if (it.size > 1) it[1] else null }
  }


  fun TomlNode.findNode(name: List<String>): TomlNode? {
    val firstName = name.firstOrNull() ?: return this
    return this.children.firstOrNull { it.name == firstName }?.findNode(name.drop(1))
  }

  configArgs.forEach { arg ->

    when (val keyValue = configToml.findNode(arg.first.split('.'))) {
      is TomlKeyValuePrimitive -> {
        println("ARG: $arg key value: ${keyValue.value} type: ${keyValue.value::class.simpleName}")

      }

      is TomlArrayOfTablesElement -> println("ARG: $arg TomlArrayOfTablesElement")
      is TomlFile -> println("ARG: $arg TomlFile")
      is TomlInlineTable -> println("ARG: $arg TomlInlineTable")
      is TomlKeyValueArray -> println("ARG: $arg KeyValueArray")
      is TomlStubEmptyNode -> println("ARG: $arg TomlStubEmptyNode")
      is TomlTable -> println("ARG: $arg TomlTable")
      null -> println("ARG: $arg not found")
    }


  }


  val serializer = DemoAppConfig.serializer()
  val d = serializer.descriptor
  println("Serial name: ${d.serialName}")
}

private fun TomlNode.applyArgs(argName: List<String>, argValue: String?) {

}
