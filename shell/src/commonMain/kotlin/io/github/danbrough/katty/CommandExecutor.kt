package io.github.danbrough.katty

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val log = kattyLog

open class CommandExecutor() :
  CoroutineContext.Element {
  // Use a SupervisorJob so that cancelling the executor's scope doesn't cancel unrelated tasks


  companion object : CoroutineContext.Key<CommandExecutor> {

    suspend operator fun minusAssign(context: CoroutineContext.Key<*>) {
      currentCoroutineContext()[CommandExecutor]?.also { executor ->
        executor.scope = CoroutineScope(executor.scope.coroutineContext.minusKey(context))
      }
    }

    suspend operator fun plusAssign(context: CoroutineContext) {
      currentCoroutineContext()[CommandExecutor]?.also { executor ->
        log.trace { "CommandExecutor::adding $context to scope: ${executor.scope}" }
        executor.scope += context
        if (context is AutoCloseable) {
          log.trace { "CommandExecutor::context is AutoClosable .. adding completion hook to close $context}" }
          executor.supervisorJob.invokeOnCompletion {
            context.close()
          }
        }
      }
    }

    suspend fun <E : CoroutineContext.Element> getOrCreate(
      key: CoroutineContext.Key<E>,
      creator: () -> E
    ): E =
      currentCoroutineContext()[CommandExecutor]?.let { executor ->
        executor.scope.coroutineContext[key] ?: creator().also {
          CommandExecutor += it
        }
      } ?: error("Expecting CommandExecutor in context")
  }

  override val key: CoroutineContext.Key<*> = CommandExecutor

  class CommandContext(val job: Job) :
    CoroutineContext.Element {
    companion object : CoroutineContext.Key<CommandContext> {
      suspend fun commandContext(): CommandContext? = currentCoroutineContext()[CommandContext]

      suspend fun <C : CoroutineContext> withCommandContext(
        context: C,
        block: suspend C.() -> Unit
      ) {
        val commandContext = commandContext() ?: error("Not running with a CommandContext")
        /*if (context is AutoCloseable) {
          commandContext.job.invokeOnCompletion {
            context.close()
          }
        }*/

        return withContext(context) {
          if (context is AutoCloseable)
            context.use {
              it.block()
            }
          else context.block()
        }
      }
    }

    override val key: CoroutineContext.Key<*> = CommandContext
  }


  val supervisorJob = SupervisorJob()
  var scope =
    CoroutineScope(supervisorJob + this)

  private val jobs = mutableListOf<Job>()
  val currentJob: Job?
    get() = jobs.lastOrNull()

  /**
   * Starts a new command. If one is already running, it will be cancelled first.
   */
  fun execute(
    //Extra context items to add to the command's scope
    context: CoroutineContext = EmptyCoroutineContext,
    command: suspend () -> Unit
  ) {
    log.trace { "CommandExecutor[${KattyUtils.threadName()}]::execute .." }

    scope.launch(context) {
      val job = currentCoroutineContext().job
      val commandContext = CommandContext(currentCoroutineContext().job)
      log.trace { "CommandExecutor::launched new command: $commandContext job: ${currentCoroutineContext().job}" }
      try {
        withContext(commandContext) {
          command()
        }
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        log.trace { "$commandContext cancelled." }
      }
    }.also { job ->
      job.invokeOnCompletion {
        jobs.remove(job)
        log.trace { "CommandExecutor[${KattyUtils.threadName()}]::removed job $job from jobs. jobCount: ${jobs.size}" }
      }
      log.trace { "CommandExecutor[${KattyUtils.threadName()}]:: added job: $job to jobs. jobCount: ${jobs.size}" }
      jobs.add(job)
    }

    //yield()
  }


  /**
   * Cancels the executor for immediate shutdown
   */
  fun cancelCurrentJob(): Boolean {
    log.trace { "CommandExecutor::cancelCurrentJob() jobCount: ${jobs.size}" }
    return currentJob?.cancel()?.let { true } ?: false
  }


  /**
   * Wait for jobs to finish
   */

  suspend fun shutdown() {
    log.trace { "CommandExecutor::shutdown() ${KattyUtils.threadName()} " }
    supervisorJob.complete()
    supervisorJob.join()
  }
}








