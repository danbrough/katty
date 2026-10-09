package io.github.danbrough.katty


typealias BasicCommandJob = suspend KattyShell.(List<String>) -> Unit

fun basicCommand(
  cmdName: String,
  helpText: String,
  job: BasicCommandJob
) = cmdName to BasicCommand(helpText, job)


fun basicCommand(
  helpText: String,
  job: BasicCommandJob
) = BasicCommand(helpText, job)

open class BasicCommand(
  private val helpText: String? = null,
  private val job: BasicCommandJob? = null
) {
  /**
   * Provide a description about this command
   */
  fun helpText(): String? = helpText

  /**
   * Invoke this command
   */
  open suspend operator fun invoke(kTerminal: KattyShell, args: List<String>) =
    job?.invoke(kTerminal, args)
}

