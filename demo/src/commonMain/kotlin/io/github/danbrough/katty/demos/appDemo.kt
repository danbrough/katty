package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyCommand
import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.kattyShell


class BasicCommand(
  commandName: String, command: suspend Context.() -> Unit
) : KattyCommand<BasicCommand.Context, Unit>(commandName, command) {

  override suspend fun context(shell: KattyShell, args: List<String>): Context =
    Context(shell, args)

  class Context(val shell: KattyShell, val args: List<String>)
}

class AppDemo() : KattyCommand<AppDemo.Context, Unit>("appDemo", Context::appDemo) {

  var count = 0

  class Context(val demo: AppDemo, val shell: KattyShell, val args: List<String>)


  override suspend fun context(shell: KattyShell, args: List<String>): Context =
    Context(this, shell, args)
}

suspend fun AppDemo.Context.appDemo() {
  shell.run {
    info("running  count=${demo.count++} args:[${args.joinToString(",")}]")
    warn("shell is: ${kattyShell()}")
  }
}