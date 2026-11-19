package io.github.danbrough.katty.command

import io.github.danbrough.katty.KattyShell

open class BaseCommand<A : Any, R : Any>(private val job: (suspend KattyShell.(arg: A) -> R)? = null) {

  open suspend fun initialize() = Unit
  open suspend operator fun invoke(shell: KattyShell, a: A): R =
    job?.invoke(shell, a) ?: error("no job provided")
}

typealias BasicCommandJob = suspend KattyShell.(List<String>) -> Unit


open class BasicCommand(
  private val helpText: String? = null, commandJob: BasicCommandJob? = null
) : BaseCommand<List<String>, Unit>(commandJob) {

  /**
   * Provide a description about this command
   */
  open fun helpText(): String? = helpText

}

fun basicCommand(
  cmdName: String, helpText: String, job: BasicCommandJob
) = cmdName to BasicCommand(helpText, job)



