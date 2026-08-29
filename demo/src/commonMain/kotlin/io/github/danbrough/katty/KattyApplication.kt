package io.github.danbrough.katty

import com.akuleshov7.ktoml.Toml
import io.github.danbrough.katty.config.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.decodeFromString
import kotlin.coroutines.CoroutineContext

object KattyApplicationKey : CoroutineContext.Key<KattyApplicationElement>

class KattyApplicationElement(val app: KattyApplication<*>) : CoroutineContext.Element {
  override val key: CoroutineContext.Key<*> = KattyApplicationKey
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> CoroutineScope.kattyApplication(): KattyApplication<T> =
  coroutineContext[KattyApplicationKey]!!.app as KattyApplication<T>

open class KattyApplication<T : Any>() {
  lateinit var config: T
}

suspend inline fun <reified T : Any> KattyApplication<T>.loadConfig(tomlPath: Path) {
  if (!SystemFileSystem.exists(tomlPath)) error("Config file $tomlPath does not exist")
  config = SystemFileSystem.resolve(tomlPath).readText().let {
    Toml.decodeFromString<T>(it)
  }
}