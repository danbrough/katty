package io.github.danbrough.katty

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CommandExecutor() :
  CoroutineContext.Element {
  // Use a SupervisorJob so that cancelling the executor's scope doesn't cancel unrelated tasks

  companion object : CoroutineContext.Key<CommandExecutor>

  override val key: CoroutineContext.Key<*> = CommandExecutor

  val supervisorJob = SupervisorJob()
  private val scope =
    CoroutineScope(supervisorJob + this)

  private var currentJob: Job? = null
  //private var isRunning = false

  /**
   * Starts a new command. If one is already running, it will be cancelled first.
   */
  fun execute(context: CoroutineContext, command: suspend () -> Unit) {

    kattyLog.trace { "CommandExecutor::execute .." }
    // Cancel any existing job before starting a new one
    //interrupt()

    //isRunning = true
    val terminal = context[KTerminal] as KTerminal

    currentJob = scope.launch(context) {
      try {
        command()
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        kattyLog.trace { "Command cancelled." }
      } finally {
        //isRunning = false
        currentJob = null
      }
    }

    //kattyLog.trace { "CommandExecutor::execute launched job" }

    /*// Wait for the command to finish. This suspends the caller.
    currentJob?.join()
    isRunning = false*/
  }


  /**
   * Cancels the executor for immediate shutdown
   */
  fun cancelCurrentJob(): Boolean = currentJob?.cancel()?.let { true } ?: false


  /**
   * Wait for jobs to finish
   */

  suspend fun shutdown() {
    kattyLog.trace { "CommandExecutor::${KattyUtils.threadName()} shutdown .." }
    supervisorJob.complete()
    supervisorJob.join()
  }
}



