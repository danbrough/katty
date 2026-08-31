package io.github.danbrough.katty

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.tree.nodes.TomlFile
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

fun Path.readText(): String =
  SystemFileSystem.source(SystemFileSystem.resolve(this)).buffered().use {
    it.readString()
  }

fun Path.parseToml(config: TomlInputConfig = TomlInputConfig()): TomlFile =
  TomlParser(config).parseString(readText())