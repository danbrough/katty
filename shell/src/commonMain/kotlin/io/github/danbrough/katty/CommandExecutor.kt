package io.github.danbrough.katty

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

private val log = logKattyShell

open class CommandExecutor(override val key: CoroutineContext.Key<*> = CommandExecutor) :
  ShellContext {
  // Use a SupervisorJob so that cancelling the executor's scope doesn't cancel unrelated tasks

  companion object : CoroutineContext.Key<CommandExecutor>

  /*  companion object : CoroutineContext.Key<CommandExecutor> {

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

    override val key: CoroutineContext.Key<*> = CommandExecutor*/

  val supervisorJob = SupervisorJob()
  val scope: CoroutineScope = CoroutineScope(supervisorJob + this)

  private var launchContext: CoroutineContext = this

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
    log.trace { "CommandExecutor[${KattyUtils.threadName()}]::execute .." }
    val commandContext = CommandContext(args)


    scope.launch(launchContext) {
      val cmdJob = currentCoroutineContext().job
      log.trace { "CommandExecutor::launched new command: $commandContext job: $cmdJob" }
      try {
        withContext(commandContext) {
          commandContext.command()
        }
      } catch (e: CancellationException) {
        // Command was cancelled, we can handle cleanup here if needed.
        // The exception is expected behavior.
        log.trace { "$commandContext cancelled." }
      }
    }.also { job ->
      commandContext.job = job
      job.invokeOnCompletion {
        jobs.remove(commandContext)
        log.trace { "CommandExecutor[${KattyUtils.threadName()}]::removed job $job from jobs. jobCount: ${jobs.size}" }
      }
      jobs.add(commandContext)
      log.trace { "CommandExecutor[${KattyUtils.threadName()}]:: added job: $job to jobs. jobCount: ${jobs.size}" }

    }

    //yield()
  }


  /**
   * Cancels the executor for immediate shutdown
   */
  fun cancelCurrentJob(): Boolean {
    log.trace { "CommandExecutor::cancelCurrentJob() jobCount: ${jobs.size}" }
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

  private val contextItems = mutableListOf<Any>()

  override fun add(element: CoroutineContext.Element) {
    launchContext += element
  }

  override fun remove(key: CoroutineContext.Key<*>) {
    launchContext = launchContext.minusKey(key)
  }

  override fun close() {
    contextItems.filterIsInstance<AutoCloseable>().forEach {
      it.close()
    }
    contextItems.clear()
    supervisorJob.complete()
  }
}








