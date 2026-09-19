@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.danbrough.katty

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

object KattyCoroutines {
  fun Job.findParentJob(clause: Job.() -> Boolean): Job? =
    if (clause(this)) this else parent?.findParentJob(clause)

  suspend fun findParentJob(clause: Job.() -> Boolean): Job? =
    currentCoroutineContext().job.findParentJob(clause)

  fun Job.parentJob(): Job = parent?.parentJob() ?: this

  suspend fun parentJob(): Job =
    currentCoroutineContext().job.let { it.findParentJob { parent == null } ?: it }

}