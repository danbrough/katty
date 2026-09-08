package io.github.danbrough.katty

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CommandExecutor(private val context: CoroutineContext = Dispatchers.Default) {
  // Use a SupervisorJob so that cancelling the executor's scope doesn't cancel unrelated tasks
  private val scope = CoroutineScope(context + SupervisorJob())
  private var currentJob: Job? = null
  private var isRunning = false

  /**
   * Starts a new command. If one is already running, it will be cancelled first.
   */
  suspend fun execute(command: suspend () -> Unit) {
    kattyLog.trace { "CommandExecutor::execute .." }
    // Cancel any existing job before starting a new one
    //interrupt()

    isRunning = true
    currentJob = scope.launch(context) {
      try {
        command()
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        println("\nCommand interrupted.")
      } finally {
        isRunning = false
        currentJob = null
      }
    }

    kattyLog.trace { "CommandExecutor::execute launched job" }

    /*// Wait for the command to finish. This suspends the caller.
    currentJob?.join()
    isRunning = false*/
  }

  /**
   * Interrupts the currently running command.
   */
  fun interrupt() {
    currentJob?.cancel()
    // Don't set isRunning = false immediately; the coroutine's finally block will handle it.
  }

  /**
   * Cleans up the executor. Should be called when the application shuts down.
   */
  fun close() {
    scope.cancel()
  }
}