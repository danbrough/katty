package io.github.danbrough.katty

import com.akuleshov7.ktoml.Toml
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.decodeFromString
import kotlin.coroutines.CoroutineContext


abstract class KattyApplication<T : Any>() : CoroutineContext.Element {
  lateinit var config: T

  companion object : CoroutineContext.Key<KattyApplication<*>>

  override val key: CoroutineContext.Key<*> = Companion
}

suspend inline fun <reified T : Any> KattyApplication<T>.loadConfig(tomlPath: Path) {
  if (!SystemFileSystem.exists(tomlPath)) error("Config file $tomlPath does not exist")
  config = SystemFileSystem.resolve(tomlPath).readText().let {
    Toml.decodeFromString<T>(it)
  }
}


suspend inline fun <reified T : KattyApplication<*>?> kattyApp(): T =
  (currentCoroutineContext()[KattyApplication] as? T)
    ?: error("Katty application of type: ${T::class.simpleName} not found")