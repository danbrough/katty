package io.github.danbrough.katty.demos

import io.github.danbrough.katty.CommandExecutor
import io.github.danbrough.katty.KTerminal
import io.github.danbrough.katty.KattyCoroutines
import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.basicCommand
import io.github.danbrough.katty.demoLog
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

val demoJobControl1 =
  basicCommand("demoJobControl1", "Test for job control", KTerminal::demoJobControl1)

suspend fun KTerminal.demoJobControl1(args: List<String>) {
  println("demoJobControl1")

  currentCoroutineContext().job.invokeOnCompletion {
    demoLog.warn { "demoJobControl1::${KattyUtils.threadName()} job cleanup" }
  }

  KattyCoroutines.parentJob().invokeOnCompletion {
    demoLog.warn { "demoJobControl1::${KattyUtils.threadName()} topJob cleanup" }
  }
}