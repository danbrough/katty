package io.github.danbrough.katty.command

import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.ShellContext
import io.github.danbrough.katty.SingleRun
import io.github.danbrough.katty.logKattyShell
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

private val log = logKattyShell

open class CommandExecutor() : ShellContext() {

  val jobs = mutableListOf<Job>()
  val currentJob: Job?
    get() = jobs.lastOrNull()

  /**
   * Starts a new command. If one is already running, it will be cancelled first.
   */
  fun execute(
    args: List<String>, singleRun: Boolean = false, command: suspend () -> Unit
  ): Job {

    val ctx = if (singleRun) SingleRun else EmptyCoroutineContext
    val job = scope.launch(ctx) {

      try {
        command()
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        log.trace { "command cancelled." }
      }
    }

    jobs.add(job)

    job.invokeOnCompletion {
      jobs.remove(job)
    }

    //log.trace { "CommandExecutor::execute [${args.joinToString(" ")}] done" }
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
    //log.trace { "CommandExecutor::supervisorJob.join()" }
    //supervisorJob.join()
    log.trace { "CommandExecutor::shutdown complete" }
  }

  override fun close() {
    log.trace { "CommandExecutor::close()" }
    super.close()
    log.trace { "CommandExecutor::supervisorJob.complete()" }
    supervisorJob.complete()
    log.trace { "CommandExecutor::close complete" }
  }
}








