package io.github.danbrough.katty.command

import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.ShellContext
import io.github.danbrough.katty.SingleRun
import io.github.danbrough.katty.logKattyShell
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val log = logKattyShell

open class CommandExecutor(initialContext: CoroutineContext = EmptyCoroutineContext) :
  ShellContext(initialContext) {

  val jobs = mutableListOf<Job>()
  val currentJob: Job?
    get() = jobs.lastOrNull()

  /**
   * Starts a new command. If one is already running, it will be cancelled first.
   */
  fun execute(
    args: List<String>,
    singleRun:Boolean = false,
    command: suspend () -> Unit
  ): Job {

    //log.trace { "CommandExecutor[${KattyUtils.threadName()}]::execute .. launchContext: $context" }
    /*    val commandContext = CommandContext(args)
        val commandContextWithName =
          CoroutineName("Command[${args.joinToString(" ")}]") + commandContext*/

    val ctx = if (singleRun) SingleRun else EmptyCoroutineContext
    val job = scope.launch(ctx) {
      /*log.error { "CommandExecutor::launch shellContext: ${currentCoroutineContext().elementList()}" }
      log.error { "shellContext: ${currentCoroutineContext()[ShellContext]}  or maybe: ${this@CommandExecutor[ShellContext]}" }*/

      //log.trace { "CommandExecutor::launched new command: $commandContext job: $cmdJob job:${supervisorJob} name:${currentCoroutineContext()[CoroutineName]}" }
      try {
        command()
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        log.trace { "command cancelled." }
      }

    }

    jobs.add(job)

    //   log.trace { "CommandExecutor[${KattyUtils.threadName()}]:: added job: $job to jobs. jobCount: ${jobs.size}" }
    job.invokeOnCompletion {
      jobs.remove(job)
      log.trace { "CommandExecutor[${KattyUtils.threadName()}]::removed job $job from jobs. jobCount: ${jobs.size} supervisorJob.active=:${supervisorJob.isActive}" }
      //commandContext.close()
    }

    log.trace { "CommandExecutor::execute [${args.joinToString(" ")}] done" }
    return job
    //yield()
  }


  /**
   * Cancels the executor for immediate shutdown
   */
  fun cancelCurrentJob(): Boolean = currentJob?.job?.cancel()?.let { true } ?: false


  /**
   * Wait for jobs to finish
   */

  suspend fun shutdown() {
    log.trace { "CommandExecutor::shutdown() ${KattyUtils.threadName()} " }
    close()
    supervisorJob.join()
    log.trace { "CommandExecutor::shutdown complete" }
  }

  override fun close() {
    log.trace { "CommandExecutor::close()" }
    super.close()
    supervisorJob.complete()
    log.trace { "CommandExecutor::close complete" }
  }
}








