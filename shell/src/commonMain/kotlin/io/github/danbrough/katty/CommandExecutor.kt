package io.github.danbrough.katty

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

private val log = logKattyShell

open class CommandExecutor() : ShellContext() {

  val supervisorJob = SupervisorJob()
  val scope: CoroutineScope = CoroutineScope(supervisorJob)

  val jobs = mutableListOf<CommandContext>()
  val currentJob: CommandContext?
    get() = jobs.lastOrNull()


  /**
   * Starts a new command. If one is already running, it will be cancelled first.
   */
  fun execute(
    args: List<String>,
    command: suspend CommandContext.() -> Unit
  ) {
    //log.trace { "CommandExecutor[${KattyUtils.threadName()}]::execute .. launchContext: $context" }
    val commandContext = CommandContext(args)

    scope.launch(commandContext + context) {
      val cmdJob = currentCoroutineContext().job
      //log.trace { "CommandExecutor::launched new command: $commandContext job: $cmdJob" }
      try {
        commandContext.command()
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        log.trace { "$commandContext cancelled." }
      }
    }.also { job ->
      commandContext.job = job
      jobs.add(commandContext)
      //   log.trace { "CommandExecutor[${KattyUtils.threadName()}]:: added job: $job to jobs. jobCount: ${jobs.size}" }
      job.invokeOnCompletion {
        jobs.remove(commandContext)
        //   log.trace { "CommandExecutor[${KattyUtils.threadName()}]::removed job $job from jobs. jobCount: ${jobs.size}" }
        commandContext.close()
      }
    }

    //yield()
  }


  /**
   * Cancels the executor for immediate shutdown
   */
  fun cancelCurrentJob(): Boolean {
    //log.trace { "CommandExecutor::cancelCurrentJob() jobCount: ${jobs.size}" }
    return currentJob?.job?.cancel()?.let { true } ?: false
  }


  /**
   * Wait for jobs to finish
   */

  suspend fun shutdown() {
    log.trace { "CommandExecutor::shutdown() ${KattyUtils.threadName()} " }
    close()
    supervisorJob.join()
  }


  override fun close() {
    //log.info { "CommandExecutor::close()" }
    super.close()
    supervisorJob.complete()
  }
}








