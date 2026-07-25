package io.github.danbrough.katty

import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.io.SystemLineSeparator
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlinx.io.writeString


open class KTerminal(val terminal: Terminal, val history: History) {
  constructor(historyFile: Path? = null) : this(Terminal(interactive = true), DefaultHistory(historyFile))

  interface History {
    val maxSize: Int
    val history: List<String>
    fun loadHistory()
    fun saveHistory()

    fun previous(): String?
    fun next(): String?

    fun addToHistory(line: String)
  }


  var cursorPos: Int = 0
  var promptLength: Int = 0

  var currentLine: String = ""

  /**
   * Must update the [promptLength] property with the character length of the prompt returned
   */
  var prompt: KTerminal.() -> String = {
    $$"$myPrompt>> ".let {
      promptLength = it.length
      TextStyles.bold(TextColors.brightGreen(it))
    }
  }

  val keyboardActions: MutableList<KeyboardAction> = mutableListOf()

  protected open fun registerDefaultKeyboardActions() =
    keyboardActions.addAll(KeyboardActions.DefaultActions)

  open fun runCommand(cmd: String) {

  }

  fun cmdLoop() {

    registerDefaultKeyboardActions()

    while (true) {
      if (cursorPos == 0) {
        terminal.rawPrint(prompt())
        cursorPos = promptLength
      }

      val firstKey = terminal.enterRawMode().use { raw ->
        raw.readKeyOrNull()
      } ?: continue


      val actionResult =
        keyboardActions.firstOrNull { it.matcher(firstKey) }?.action?.invoke(this, firstKey)
      when (actionResult) {
        KeyboardActionResult.EXIT -> return
        KeyboardActionResult.CONTINUE -> continue
        KeyboardActionResult.ADD_TO_LINE, null -> {}
      }

      if (firstKey.key.length == 1) {
        terminal.print(firstKey.key)
        currentLine += firstKey.key
        cursorPos++
      } else {
        terminal.println(terminal.theme.danger("${SystemLineSeparator}Unknown key: ${firstKey.key}"))
        cursorPos = 0
        currentLine = ""
      }
    }
  }

  fun run() {
    history.loadHistory()
    runCatching {
      cmdLoop()
    }.exceptionOrNull().also { err ->
      runCatching {
        history.saveHistory()
      }.exceptionOrNull()?.also {
        it.printStackTrace()
      }
      if (err != null) throw err
    }
  }
}


open class DefaultHistory(
  val historyFile: Path? = null,
  override val maxSize: Int = 1000,
  override val history: MutableList<String> = mutableListOf()
) : KTerminal.History {

  private var index: Int = 0

  override fun loadHistory() {
    if (historyFile == null || !SystemFileSystem.exists(historyFile)) return
    SystemFileSystem.source(historyFile).buffered().use { source ->
      while (true) {
        source.readLine()?.also {
          history.add(it)
          if (history.size > maxSize)
            history.removeFirst()
        } ?: break
      }
    }
    index = history.size
  }

  override fun saveHistory() {
    historyFile ?: return
    SystemFileSystem.sink(historyFile).buffered().use { sink ->
      history.forEach {
        sink.writeString("$it$SystemLineSeparator")
      }
    }
  }

  override fun previous(): String? =
    if (index > 0) {
      index--
      return history[index]
    } else null

  override fun next(): String? = if (index < history.size - 1) {
    index++
    history[index]
  } else null

  override fun addToHistory(line: String) {
    history.add(line)
    if (history.size > maxSize)
      history.removeFirst()
    index = history.size
  }
}

