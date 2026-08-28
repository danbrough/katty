package io.github.danbrough.katty.config

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.tree.nodes.TomlFile
import com.akuleshov7.ktoml.tree.nodes.TomlNode
import com.akuleshov7.ktoml.tree.nodes.TomlTable
import com.akuleshov7.ktoml.writers.TomlWriter
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.HorizontalRule
import io.github.danbrough.katty.Bashy
import io.github.danbrough.katty.BasicCommand
import io.github.danbrough.katty.BasicCommandHandler
import io.github.danbrough.katty.KTerminal
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlinx.io.readString
import kotlinx.io.writeString

fun BasicCommandHandler.registerConfigCommands() {
  registerCommands(
    "configDemo" to DemoConfigCommand,
    "configArgsDemo" to DemoConfigArgs,
  )
}

val DemoConfigCommand = BasicCommand("config demo") {
  println("config demo")
  mergeDemo()
  /*

    val parser = TomlParser(TomlInputConfig())

    val toml: String.() -> TomlFile = parser::parseString


    val content1 = """
      # This is a TOML document
      title = "TOML Example"

      [owner]
      name = "Tom Preston-Werner"
      dob = 1979-05-27T07:32:00-08:00

      [database]
      enabled = true
      ports = [ 8000, 8001, 8002 ]
      data = [ ["delta", "phi"], [3.14] ]
      temp_targets = { cpu = 79.5, case = 72.0 }

      [servers]

      [servers.alpha]
      ip = "10.0.0.1"
      role = "frontend"

      [servers.beta]
      ip = "10.0.0.2"
      role = "backend"
    """.trimIndent().toml()

    val content2 = """
      [content2]
      name = "Freddy Gruēgũr"

    """.trimIndent().toml()


    terminal.printTomlFile("Content1", content1)
    terminal.printTomlFile("Content2", content2)


    tomlMerge(content1, content2)
    terminal.printTomlFile("Merged", content1)
  */


  /*  val files =
      listOf(
        "demo/src/commonMain/resources/stuff1.toml",
        "demo/src/commonMain/resources/stuff2.toml"
      ).map { Path(it) }


    val f1 = parser.parseLines(files[0].toLines())
    val f2 = parser.parseLines(files[1].toLines())

    terminal.printFile("f1", f1)
    terminal.printFile("f2", f2)

    f1.children.addAll(f2.children)

    terminal.printFile("merged", f1)*/
}


fun KTerminal.mergeDemo() {
  val parser = TomlParser(TomlInputConfig())

  val files =
    listOf(
      "demo/src/commonMain/resources/example1.toml",
      "demo/src/commonMain/resources/example2.toml",
      "demo/src/commonMain/resources/example_merged.toml"
    ).map { Path(it) }
  val ex1 = parser.parseLines(files[0].toLines())
  val ex2 = parser.parseLines(files[1].toLines())

  terminal.printTomlFile("Example 1", ex1)
  terminal.printTomlFile("To Merge", ex2)
  tomlMerge(ex1, ex2)
  val writer = TomlWriter(TomlOutputConfig(explicitTables = true))
  val mergedContent = writer.writeToString(ex1)
  terminal.printTomlFile("Merged", ex1)
  SystemFileSystem.sink(files[2]).buffered().use {
    it.writeString(mergedContent)
  }
}

fun tomlMerge(first: TomlNode, second: TomlNode) {
  second.children.forEachIndexed { index, childToMerge ->
    val match = first.children.firstOrNull { it.name == childToMerge.name }
    if (match == null) {
      first.children.add(childToMerge)
    } else {
      if (childToMerge is TomlTable) {
        tomlMerge(match, childToMerge)
      } else {
        first.children.add(index, childToMerge)
        first.children.remove(match)
      }
    }
  }
}


fun Path.toLines(): Sequence<String> = sequence {
  SystemFileSystem.source(SystemFileSystem.resolve(this@toLines)).buffered().use {
    while (true) {
      yield(it.readLine() ?: return@sequence)
    }
  }
}


fun Path.readText(): String =
  SystemFileSystem.source(SystemFileSystem.resolve(this)).buffered().use {
    it.readString()
  }

fun Path.parseToml(config: TomlInputConfig = TomlInputConfig()): TomlFile =
  TomlParser(config).parseString(readText())


val printTomlFile: Terminal.(String, TomlFile) -> Unit = { caption, file ->
  val writer = TomlWriter(TomlOutputConfig(explicitTables = true))
  printSectionTitle("$caption text")
  println(TextColors.rgb("#dd33AA")(writer.writeToString(file)))
  printSectionTitle(caption)
  println(Bashy.Theme.normal(file.prettyStr()))
}

fun TomlNode.tomlString(config: TomlOutputConfig = TomlOutputConfig()): String {
  val writer = TomlWriter(config)
  val file = this as? TomlFile ?: TomlFile().also { it.children.add(this@tomlString) }
  return writer.writeToString(file)
}

fun Terminal.printSectionTitle(title: String,content:Any? = null) {
  print(Caption(HorizontalRule(), bottom = (TextStyles.bold + TextColors.brightCyan)(title)))
  if (content != null)
    println(TextColors.green(content.toString()))
}


