package io.github.danbrough.katty.demos

import io.github.danbrough.katty.ContextProvider
import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextGetOrNull
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


class MySpecialContext(override val key: CoroutineContext.Key<*> = MySpecialContext) :
  CoroutineContext.Element {
  companion object : CoroutineContext.Key<MySpecialContext>

  var count = 0
}

val mySpecialContextProvider = ContextProvider(MySpecialContext) {
  MySpecialContext()
}


val ContextDemo = basicCommand("context", "Context demos") {
  val args = it.drop(1)

  val shell = shellContext()

  val stuff = shellContext(Stuff) {
    log.info { "creating a stuff" }
    Stuff("stuff")
  }
  log.debug { "shellContext: $shell" }
  info("stuff: $stuff")

  val provider = shellContextGetOrNull<ContextProvider<MySpecialContext>>(MySpecialContext)

  success("ran context demo")
}

