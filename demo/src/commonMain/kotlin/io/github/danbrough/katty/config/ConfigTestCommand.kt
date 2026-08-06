package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.horizontalLayout
import com.github.ajalt.mordant.widgets.Spinner
import io.github.danbrough.katty.CommandHandler
import io.github.danbrough.katty.KTerminal
import io.github.danbrough.katty.KattyUtils


object ConfigTestCommand : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == "configTest"

  override fun helpText(): String = "configTest: config demo"
  override fun exec(
    kTerminal: KTerminal,
    args: List<String>
  ) = kTerminal.configTest(args)
}


class HashtableConfigSource(
  val map: MutableMap<String, String> = mutableMapOf()
) : MutableConfigSource {
  override fun getProperty(name: String): String? = map[name]
  override fun setProperty(name: String, value: String) {
    map[name] = value
  }

  override fun hasProperty(name: String): Boolean = map.contains(name)
}

class EnvConfigSource(val nameMap: (String) -> String = { it.uppercase().replace('.', '_') }) :
  ConfigSource {
  override fun getProperty(name: String): String? =
    KattyUtils.getEnv(nameMap(name))

  override fun hasProperty(name: String): Boolean = getProperty(name) != null
}



@Suppress("SpellCheckingInspection")
private fun KTerminal.configTest(args: List<String>) {
  val mapConfig = HashtableConfigSource()
  val envConfig = EnvConfigSource()
  val config = mapConfig.chain(envConfig)
  if (!config.hasProperty("thang.message")) config["thang.message"] =
    "The default thang.message property. Override by setting the environmental variable THANG_MESSAGE"
  println("home: ${config["home"]}")
  config["home"] = "/home/new_home_set_in_config"
  println("home: ${config["home"]}")
  println("hosttype: ${config["hosttype"]} ostype: ${config["ostype"]} java.home: ${config["java.home"]}")
  args.drop(1).forEach {
    println("$it: ${TextColors.yellow(config[it] ?: "not set")}")
  }



}