package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.shellContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


private val log = logDemo

class Stuff(val name: String) : AbstractCoroutineContextElement(Stuff), KattyContext {
  override fun close() {
    log.debug { "Stuff::close() name:$name" }
  }

  override fun toString(): String {
    return "Stuff[name=$name]"
  }

  companion object : CoroutineContext.Key<Stuff>
}

val ContextDemo = basicCommand("context", "Context demos") {
  val shell = shellContext()

  val stuff = shellContext(Stuff) {
    log.info { "creating a stuff" }
    Stuff("stuff")
  }
  log.debug { "shellContext: $shell" }
  info("stuff: $stuff")
  success("ran context demo")
}

