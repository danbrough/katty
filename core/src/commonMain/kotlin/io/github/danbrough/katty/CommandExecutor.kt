package io.github.danbrough.katty

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CommandExecutor(context: CoroutineContext = Dispatchers.Default) :
  CoroutineContext.Element {
  // Use a SupervisorJob so that cancelling the executor's scope doesn't cancel unrelated tasks

  companion object : CoroutineContext.Key<CommandExecutor>
  override val key: CoroutineContext.Key<*> = CommandExecutor

  val supervisorJob = SupervisorJob()
  private val scope=
    CoroutineScope(context + supervisorJob + this)

  private var currentJob: Job? = null
  //private var isRunning = false

  /**
   * Starts a new command. If one is already running, it will be cancelled first.
   */
  fun execute(command: suspend () -> Unit) {
    kattyLog.trace { "CommandExecutor::execute .." }
    // Cancel any existing job before starting a new one
    //interrupt()

    //isRunning = true
    currentJob = scope.launch {
      try {
        command()
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        println("\nCommand interrupted.")
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
  fun cancel() {
    scope.cancel()
  }

  /**
   * Wait for jobs to finish
   */

  suspend fun shutdown() {
    kattyLog.trace { "CommandExecutor::${KattyUtils.threadName()} shutdown .." }
    supervisorJob.complete()
    supervisorJob.join()
  }



}



