package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyCommand.Context
import io.github.danbrough.katty.KattyCommandBasic
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.get
import io.github.danbrough.katty.kattyShellOrNull
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.shellContextOrNull


val AppDemoCommand = KattyCommandBasic(::AppDemoContext) {
  success("args:[${args.joinToString(",")}]")
  info("kattyShell: ${kattyShellOrNull()}")
  info("shellContext: ${shellContextOrNull()}")
  muted("context message: ${ContextMessage.get()?.message}")
  shellContextOrNull()?.addToContext(ContextMessage("The count was: ${app.count}"))
  success("count: ${app.count++}")
}


class AppDemo() {
  var count = 1
}


class AppDemoContext(shell: KattyShell, args: List<String>) : Context(shell, args) {
  companion object {
    val appDemo by lazy {
      logDemo.error { "CREATING APP DEMO" }
      AppDemo()
    }
  }

  val app = appDemo
}
