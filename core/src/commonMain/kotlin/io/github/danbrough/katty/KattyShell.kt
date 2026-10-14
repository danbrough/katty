package io.github.danbrough.katty

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/*
// 1. Define an abstract base class extending AbstractCoroutineContextElement
abstract class MyCustomElement : AbstractCoroutineContextElement(MyCustomElement) {

  // 2. Define a companion object acting as the singleton key for this base type
  companion object Key : CoroutineContext.Key<MyCustomElement>

  // Abstract property or method your subclasses will implement
  abstract val identifier: String
}

// 3. Create subclasses extending your custom element base class
class AuthElement(val token: String) : MyCustomElement() {
  override val identifier: String = "Auth"
}

class AnalyticsElement(val trackingId: String) : MyCustomElement() {
  override val identifier: String = "Analytics"
}

suspend fun testElement() {
  // Creating a context with one of your subclasses
  val authContext = CoroutineName("MyScope") + AuthElement("secret_token_123")
  coroutineScope {
    launch(authContext) {
      // Retrieve using the base class Key singleton
      val element = coroutineContext[MyCustomElement]

      if (element is AuthElement) {
        println("Found Auth token: ${element.token}") // Output: Found Auth token: secret_token_123
      }

      // If you chain a different subclass, it replaces the existing one under that Key
      withContext(AnalyticsElement("analytics_id_abc")) {
        val nestedElement = coroutineContext[MyCustomElement]
        println("Nested identifier: ${nestedElement?.identifier}") // Output: Nested identifier: Analytics
      }
    }
  }
}
*/


abstract class KattyShell() : AbstractCoroutineContextElement(KattyShell) {
  companion object : CoroutineContext.Key<KattyShell>

  var cursorPos: Int = 0
  var promptLength: Int = 0

  var currentLine: StringBuilder = StringBuilder()
  val linePos: Int
    get() = cursorPos - promptLength

  abstract fun warn(message: String)
  abstract fun muted(message: String)
  abstract fun danger(message: String)
  abstract fun success(message: String)
  abstract fun info(message: String)
  abstract fun print(message: String)
  abstract fun println(message: String)
  abstract fun println()

  abstract suspend fun printPrompt(newLine: Boolean = true)

  abstract suspend fun runCommand(args: List<String>, singleCommandRun: Boolean = false)
}