package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.tree.nodes.TomlFile
import com.akuleshov7.ktoml.tree.nodes.TomlNode
import com.akuleshov7.ktoml.writers.TomlWriter
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.HorizontalRule
import io.github.danbrough.katty.Bashy
import io.github.danbrough.katty.BasicCommand
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine

val ConfigDemoCommand = BasicCommand("config demo") {
  println("config demo")
  val files =
    listOf(
      "demo/src/commonMain/resources/stuff1.toml",
      "demo/src/commonMain/resources/stuff2.toml"
    ).map { Path(it) }

  val parser = TomlParser(TomlInputConfig())
  val writer = TomlWriter(TomlOutputConfig(explicitTables = false))

  fun printFile(name: String, f: TomlFile) {
    terminal.println(Caption(HorizontalRule(), bottom = name))
    println(Bashy.Theme.normal(f.prettyStr()))
    println(TextColors.rgb("#dd3355")(writer.writeToString(f)))
  }

  val f1 = parser.parseLines(files[0].toLines())
  val f2 = parser.parseLines(files[1].toLines())


  printFile("f1", f1)
  printFile("f2", f2)

  TomlNode
  f1.children.addAll(f2.children)

  printFile("merged", f1)
}


fun Path.toLines(): Sequence<String> = sequence {
  SystemFileSystem.source(SystemFileSystem.resolve(this@toLines)).buffered().use {
    while (true) {
      yield(it.readLine() ?: return@sequence)
    }
  }
}