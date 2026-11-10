package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.command.basicCommand
import io.github.danbrough.katty.kattyShell
import io.github.danbrough.katty.logDemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val log = logDemo
val LaunchDemoCommand = basicCommand("launch", "launches a coroutine") {
  launchDemo(it.drop(1))
}

val launchScope = CoroutineScope(SupervisorJob())

private suspend fun KattyShell.launchDemo(args: List<String>) {
  info("running launch demo ..")
  val shellScope = kattyShell().shellScope
  muted("shellScope: $shellScope")
  val job = launchScope.launch(KattyUtils.ioDispatcher) {
    info("started launched job ..")
    delay(5.seconds)
    info("launched job finished")
  }
  info("launch demo finished jobActive:${job.isActive}")
}