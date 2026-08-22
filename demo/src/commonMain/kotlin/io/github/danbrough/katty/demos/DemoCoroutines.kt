package io.github.danbrough.katty.demos

import io.github.danbrough.katty.BasicCommand
import io.github.danbrough.katty.KTerminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

val DemoCoroutinesCommand = BasicCommand("coroutine demo") {
  demoCoroutines()
}


class Session(val name: String)

fun <T : CoroutineContext.Element> createKey(): CoroutineContext.Key<T> =
  object : CoroutineContext.Key<T> {}


object SessionKey : CoroutineContext.Key<MyElement<Session>>
class MyElement<T>(val terminal: KTerminal, val t: T) : AutoCloseable,
  CoroutineContext.Element {
  override val key: CoroutineContext.Key<*> = SessionKey
  override fun close() {
    terminal.println("MyElement::Closing session")
  }
}


suspend fun KTerminal.demoCoroutines() {
  val sessionElement = MyElement(this, Session("This is my session"))

  coroutineScope {
    launch(sessionElement + Dispatchers.Default) {
      val session: Session? = currentCoroutineContext()[SessionKey]?.t
      println("Session: $session")
      coroutineScope {
        launch(Dispatchers.Default) {
          delay(5.seconds)
          println("inner coroutine finishing")
        }
        println("outer coroutine finishing")
      }
    }.invokeOnCompletion {
      println("closing sessionElement..")
      sessionElement.close()
    }
    delay(1.seconds)
    println("first scope at end")
  }
}


