package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyContext
import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.kattyCommandContextDefault
import io.github.danbrough.katty.kattyShell
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


class MySpecialContext(override val key: CoroutineContext.Key<*> = MySpecialContext) :
  CoroutineContext.Element {
  companion object : CoroutineContext.Key<MySpecialContext>

  var count = 0
}

val mySpecialContextProvider = {
  log.error { "CREATING MY SPECIAL CONTEXT" }
  MySpecialContext()
}


var firstTime = true

val ContextDemo = basicCommand("context", "Context demos") {
  val args = input
  info("args: $args")

  val shell = shellContext()
  info("shell: $shell")
  info("terminal: ${kattyShell()}")
  info("commandContext: ${kattyCommandContextDefault()}")

  if (firstTime) {
    shellContext.addToContext(MySpecialContext, mySpecialContextProvider)
    firstTime = false
  }

  val stuff = shellContext(Stuff) {
    log.info { "creating a stuff" }
    Stuff("stuff")
  }

  info("stuff: $stuff")

  val myContext = shellContext(MySpecialContext)
  info("my special context.count = ${myContext.count}")
  myContext.count++

  success("ran context demo")
}

