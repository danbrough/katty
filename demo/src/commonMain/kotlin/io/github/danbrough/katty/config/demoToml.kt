package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.writers.TomlWriter
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import io.github.danbrough.katty.BasicCommand
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

val TOML_TEST_DATA = """
  
    #@ENV LOG_LEVEL
    log-level = 2
    sections = [
      { message = "Message for Section A" },
      { message = "Message for Section B" },
    ]
   
""".trimIndent()

@Serializable
data class TomlConfig(@SerialName("log-level") val logLevel: Int, val sections: List<Section> = emptyList()){

  @Serializable
  data class Section(val message: String)
}

val DemoTomlCommand = BasicCommand("demoToml", "some tests for toml") { args ->
  println(terminal.theme.info("TOML TEST Running"))

  val inputConfig = TomlInputConfig(ignoreUnknownNames = true)
  val tomlFile = TomlParser(inputConfig).parseString(TOML_TEST_DATA)
  terminal.println("tomlFile: " + tomlFile.prettyStr())
  val tomlString = TomlWriter(TomlOutputConfig()).writeToString(tomlFile)
  terminal.println(TextColors.green(tomlString))

  val key = args[0]
  println("key: $key")

  tomlFile.children.firstOrNull { it.name == key }?.also {
    println("found: $it")
  }

  val toml = Toml(inputConfig)
  println("tomlConfig: ${TextColors.green(toml.decodeFromString<TomlConfig>(tomlString).toString())}")
}