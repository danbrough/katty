package io.github.danbrough.katty.demos

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Caption
import com.github.ajalt.mordant.widgets.HorizontalRule
import io.github.danbrough.katty.BasicCommand
import io.github.danbrough.katty.KTerminal

val DemoThemeCommand = BasicCommand("theme demo") {
  demoTheme()
}

fun KTerminal.demoTheme() {

  val original = terminal.theme
  val newTheme = Theme(terminal.theme) {

    styles["danger"] = original.style("danger").plus(TextStyles.bold).plus(TextColors.brightRed)
    styles["info"] = original.style("info") + TextStyles.bold
    styles["warning"] = original.style("warning") + TextStyles.bold
    styles["success"] = original.style("success") + TextStyles.bold
  }


  fun printBasic(theme: Theme, caption: String) {
    terminal.println(Caption(HorizontalRule(), bottom = caption, bottomAlign = TextAlign.LEFT))


    val msg = "example message to display .. #*$&^@&$*#^(E#$&^#)*@$  public fun example(){}"
    println(theme.muted("muted $msg"))
    println(theme.warning("warning $msg"))
    println(theme.danger("danger $msg"))
    println(theme.info("info $msg"))
    println(theme.success("success $msg"))

  }

  printBasic(original, "Original Theme")

  printBasic(newTheme, "New Theme")

  terminal = Terminal(theme = newTheme)

}