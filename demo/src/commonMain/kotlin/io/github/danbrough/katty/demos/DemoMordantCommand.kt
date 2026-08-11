package io.github.danbrough.katty.demos


import com.github.ajalt.colormath.model.Oklab
import com.github.ajalt.colormath.model.SRGB
import com.github.ajalt.colormath.transform.interpolator
import com.github.ajalt.colormath.transform.sequence
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.Companion.hyperlink
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.rendering.TextStyles.italic
import com.github.ajalt.mordant.rendering.TextStyles.strikethrough
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.VerticalAlign
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.horizontalLayout
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.UnorderedList
import com.github.ajalt.mordant.widgets.progress.build
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.speed
import com.github.ajalt.mordant.widgets.progress.text
import com.github.ajalt.mordant.widgets.progress.timeRemaining
import com.github.ajalt.mordant.widgets.withPadding
import io.github.danbrough.katty.BasicCommand
import kotlin.time.TimeSource

private val shadowColor = rgb("#24218c")

private val green = rgb("#2fb479")
private val blue = rgb("#2f61b4")
private val magenta = rgb("#b02fb4")
private val orange = rgb("#b4832f")

val DemoMordantCommand =
  BasicCommand("mordant demo") {
    demoMordant(terminal)
  }

fun demoMordant(
  terminal: Terminal = Terminal(theme = Theme {
    styles["hr.rule"] = shadowColor
    styles["panel.border"] = shadowColor
  })
) {


  val layout = table {
    borderType = BorderType.BLANK
    column(0) { width = ColumnWidth.Fixed(55) }
    body {
      row {
        cell(titleExample()) {
          columnSpan = 2
          align = TextAlign.CENTER
        }
      }
      row {
        cell(
          Panel(
            featuresExample().withPadding { horizontal = 2; vertical = 1 },
            Text(brightRed("Colorful styling for command-line applications")),
            expand = true
          )
        ) { columnSpan = 2 }
      }
      row {
        cell(
          Panel(
            tableExample().withPadding { vertical = 1; horizontal = 2 },
            Text(brightRed("Tables")),
            expand = true
          )
        ) { rowSpan = 2 }
        cell(
          Panel(
            cjkExample(),
            brightRed("Asian Languages and Emoji"),
            expand = true
          ),
        )
      }
      row {
        cell(
          Panel(
            progressExample().withPadding(1),
            Text(brightRed("Animations and Progress Bars")),
            expand = true
          ),
        )
      }
      row {
        cell(
          Panel(
            markdownExample().withPadding { vertical = 1; left = 4 },
            Text(brightRed("Render Markdown directly in your terminal")),
            expand = true
          )
        ) { columnSpan = 2 }
      }
    }
  }

  terminal.println("\n\n")
  terminal.println(layout)

}

private fun featuresExample() = UnorderedList(
  orange("Color text with themed ANSI colors or full RGB values"),
  magenta(
    "Add styles like ${italic("italic")}, " +
        "${(blue + hyperlink("www.example.com"))("hyperlinks")}, " +
        "and ${strikethrough("strikethrough")} "
  ),
  green(
    "Colors are automatically downsampled to what your terminal supports"
  )
)

private fun cjkExample(): String {
  return """
    |
    | 🚀你好世界
    | 🙌こんにちは世界
    | 👍안녕하세요 세계
    |
    """.trimMargin()
}

private fun progressExample() = progressBarLayout(spacing = 1) {
  text("file.iso")
  progressBar()
  speed("B/s")
  timeRemaining()
}.build(30000000000, 25000000000, TimeSource.Monotonic.markNow(), speed = 351.0)


private const val rightArrow = """
      │╲
  ╭───┘ ╲
  ╰───┐ ╱
      │╱
"""

private val markdown = """
# Markdown

- Supports all of GFM markdown
- Including lists and `inline` *styles*

| Tables | Work | Too |
|--------|:----:|----:|
| 1      | 2    | 3   |
""".trim()

private fun markdownExample() = horizontalLayout {
  column(0) { width = ColumnWidth.Auto }
  column(1) { width = ColumnWidth.Auto }
  column(2) { width = ColumnWidth.Fixed(50) }
  cell(Panel(Text(dim(markdown)).withPadding(2)))
  cell(brightRed(rightArrow)) { verticalAlign = VerticalAlign.MIDDLE }
  cell(Panel(Markdown(markdown).withPadding {
    vertical = 1; horizontal = 2
  }))
}


private fun tableExample() = table {
  borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
  tableBorders = Borders.NONE
  borderStyle = rgb("#4b25b9")
  column(0) { style = green }
  column(1) { style = blue }
  column(2) { style = magenta }
  column(3) { style = orange }

  header {
    style = italic + bold
    row("Airport", "Location", "Code", "Passengers")
  }
  body {
    rowStyles(TextStyle(), dim.style)
    row("Hartsfield–Jackson", "Georgia", "ATL", "93,699,630")
    row("Dallas/Fort Worth", "Texas", "DFW", "73,362,946")
    row("Denver", "Colorado", "DEN", "69,286,461")
    row("O'Hare", "Illinois", "ORD", "68,340,619")
  }
}

private fun titleExample(): String {
  val title = """
███╗   ███╗ ██████╗ ██████╗ ██████╗  █████╗ ███╗   ██╗████████╗
████╗ ████║██╔═══██╗██╔══██╗██╔══██╗██╔══██╗████╗  ██║╚══██╔══╝
██╔████╔██║██║   ██║██████╔╝██║  ██║███████║██╔██╗ ██║   ██║   
██║╚██╔╝██║██║   ██║██╔══██╗██║  ██║██╔══██║██║╚██╗██║   ██║   
██║ ╚═╝ ██║╚██████╔╝██║  ██║██████╔╝██║  ██║██║ ╚████║   ██║   
╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝   ╚═╝   
""".trim('\n')
  return buildString {
    for (line in title.lineSequence()) {
      val lerp = Oklab.interpolator {
        stop(SRGB("#e74856"))
        stop(SRGB("#9648e7"))
      }.sequence(line.length)
      line.asSequence().zip(lerp).forEach { (c, color) ->
        append(TextColors.color(color)(c.toString()))
      }
      append("\n")
    }
  }.replace(Regex("""[╔═╗║╚╝]""")) { shadowColor(it.value) }
}
