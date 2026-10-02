package io.github.danbrough.katty

import com.github.ajalt.colormath.model.RGB
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.rendering.Theme

private val DEFAULT_HEADER = RGB("#c678dd")
private val DEFAULT_HIGHLIGHT = RGB("#82AAFF")
private val DEFAULT_GRAY = RGB("#5c6370")
private val DEFAULT_RED = RGB("#FF757F")
private val DEFAULT_YELLOW = RGB("#C792EA")
private val DEFAULT_GREEN = RGB("#4DD0C2")
val DEFAULT_STYLE = TextStyle(
  color = null,
  bgColor = null,
  bold = false,
  italic = false,
  underline = false,
  dim = false,
  inverse = false,
  strikethrough = false,
  hyperlink = null,
)
val KattyTheme = Theme {
  styles.putAll(
    mapOf(
      "success" to TextStyle(DEFAULT_GREEN) + TextStyles.bold,
      "danger" to TextStyle(DEFAULT_RED),
      "warning" to TextStyle(DEFAULT_YELLOW),
      "info" to TextStyle(DEFAULT_GREEN),
      "muted" to TextStyle(dim = true),

      "list.number" to DEFAULT_STYLE,
      "list.bullet" to DEFAULT_STYLE,
      "hr.rule" to DEFAULT_STYLE,
      "panel.border" to DEFAULT_STYLE,

      "prompt.prompt" to DEFAULT_STYLE,
      "prompt.default" to TextStyle(DEFAULT_HIGHLIGHT, bold = true),
      "prompt.choices" to TextStyle(DEFAULT_HIGHLIGHT, bold = true),
      "prompt.choices.invalid" to TextStyle(DEFAULT_RED),

      "progressbar.pending" to TextStyle(DEFAULT_GRAY),
      "progressbar.complete" to TextStyle(DEFAULT_HIGHLIGHT),
      "progressbar.indeterminate" to TextStyle(DEFAULT_HIGHLIGHT),
      "progressbar.separator" to DEFAULT_STYLE,
      "progressbar.finished" to TextStyle(DEFAULT_GREEN),

      "markdown.blockquote" to TextStyle(DEFAULT_YELLOW),
      "markdown.emph" to TextStyle(italic = true),
      "markdown.strong" to TextStyle(bold = true),
      "markdown.stikethrough" to TextStyle(strikethrough = true),
      "markdown.code.block" to TextStyle(DEFAULT_HIGHLIGHT),
      "markdown.code.span" to TextStyle(DEFAULT_HIGHLIGHT, DEFAULT_GRAY),
      "markdown.table.header" to TextStyle(bold = true),
      "markdown.table.body" to DEFAULT_STYLE,
      "markdown.link.text" to TextStyle(DEFAULT_HIGHLIGHT),
      "markdown.link.destination" to TextStyle(DEFAULT_HIGHLIGHT, dim = true),
      "markdown.img.alt-text" to TextStyle(dim = true),
      "markdown.h1" to TextStyle(DEFAULT_HEADER, bold = true),
      "markdown.h2" to TextStyle(DEFAULT_HEADER, bold = true),
      "markdown.h3" to TextStyle(DEFAULT_HEADER, bold = true, underline = true),
      "markdown.h4" to TextStyle(DEFAULT_HEADER, underline = true),
      "markdown.h5" to TextStyle(DEFAULT_HEADER, italic = true),
      "markdown.h6" to TextStyle(DEFAULT_HEADER, dim = true),

      "select.title" to TextStyle(DEFAULT_HEADER, bold = true),
      "select.cursor" to TextStyle(DEFAULT_HIGHLIGHT),
      "select.selected" to TextStyle(DEFAULT_GREEN),
      "select.unselected-title" to DEFAULT_STYLE,
      "select.unselected-marker" to TextStyle(dim = true),
    )
  )
  strings.putAll(
    mapOf(
      "list.number.separator" to ".",
      "list.bullet.text" to "•",
      "progressbar.pending" to "━",
      "progressbar.complete" to "━",
      "progressbar.separator" to " ",
      "hr.rule" to "─",

      "markdown.task.checked" to "☑",
      "markdown.task.unchecked" to "☐",
      "markdown.h1.rule" to "═",
      "markdown.h2.rule" to "─",
      "markdown.h3.rule" to " ",
      "markdown.h4.rule" to " ",
      "markdown.h5.rule" to " ",
      "markdown.h6.rule" to " ",
      "markdown.blockquote.bar" to "▎",

      "select.cursor" to "❯",
      "select.selected" to "✓",
      "select.unselected" to "•",
    )
  )

  flags.putAll(
    mapOf(
      "progressbar.pulse" to true,

      "markdown.code.block.border" to true,
      "markdown.table.ascii" to false,
    )
  )
  dimensions.putAll(
    mapOf(
      "hr.title.padding" to 1,
      "panel.title.padding" to 1,
      "markdown.header.padding" to 1,
    )
  )
}
