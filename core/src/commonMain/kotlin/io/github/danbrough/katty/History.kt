package io.github.danbrough.katty

import kotlinx.io.SystemLineSeparator
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlinx.io.writeString
interface History {
  val maxSize: Int
  val history: List<String>
  fun loadHistory()
  fun saveHistory()

  fun previous(): String?
  fun next(): String?

  fun addToHistory(line: String)
}
open class DefaultHistory(
  val historyFile: Path? = null,
  override val maxSize: Int = 1000,
  override val history: MutableList<String> = mutableListOf()
) : History {

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
