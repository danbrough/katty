package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KattyShell
import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.isSingleRun
import io.github.danbrough.katty.kattyShell
import io.github.danbrough.katty.logDemo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val log = logDemo
val LaunchDemoCommand = basicCommand("launch", "launches a coroutine") {
  launchDemo(input.drop(1))
}


private suspend fun KattyShell.launchDemo(args: List<String>) {
  info("running launch demo .. singleRun:${isSingleRun()}")
  val shellScope = kattyShell().shellContext.scope
  muted("shellScope: $shellScope")
  val job = shellScope.launch(KattyUtils.ioDispatcher) {
    info("started launched job ..")
    delay(5.seconds)
    info("launched job finished")
  }
  info("launch demo finished jobActive:${job.isActive}")
}