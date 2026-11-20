package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyCommand.Context
import io.github.danbrough.katty.KattyCommandBasic
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.kattyShellOrNull
import io.github.danbrough.katty.logDemo
import io.github.danbrough.katty.shellContext
import io.github.danbrough.katty.shellContextAdd
import io.github.danbrough.katty.shellContextOrNull


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


object AppDemoCommand : KattyCommandBasic<AppDemoContext>(::AppDemoContext) {
  override suspend fun initialize() {
    logDemo.warn { "initializing AppDemoCommand" }
    shellContextAdd(ContextMessage("The count is not set"))
  }

  override suspend fun invoke(context: AppDemoContext) {
    context.apply {
      success("args:[${args.joinToString(",")}]")
      info("kattyShell: ${kattyShellOrNull()}")
      info("shellContext: ${shellContextOrNull()}")
      muted("context message: ${shellContext(ContextMessage)?.message}")
      shellContextAdd(ContextMessage("The count was: ${app.count}"))
      success("count: ${app.count++}")
    }
  }

}