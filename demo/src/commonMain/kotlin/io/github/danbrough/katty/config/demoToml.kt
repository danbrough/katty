package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.writers.TomlWriter
import io.github.danbrough.katty.BasicCommand
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val TOML_TEST_DATA = """
  log-level = 2
  
#  [section.a]
#  message = "Message for section A"
#  [section.b] 
#  message = "Message for section B"
""".trimIndent()

@Serializable
data class TomlConfig(@SerialName("log-level") val logLevel: Int)

val DemoTomlCommand = BasicCommand("tomlDemo", "some tests for toml") {
  println(terminal.theme.info("TOML TEST Running"))

  val tomlFile = TomlParser(TomlInputConfig()).parseString(TOML_TEST_DATA)
  terminal.println("tomlFile: " + tomlFile.prettyStr())
  val tomlString = TomlWriter(TomlOutputConfig()).writeToString(tomlFile)
  terminal.println(tomlString)


}