package io.github.danbrough.katty

import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

interface CoroutineScoped

open class CoroutineKey<T : CoroutineScoped>(val content: T? = null) :
  CoroutineContext.Key<CoroutineElement<T>>

suspend fun <T : CoroutineScoped> scopedValue(key: CoroutineKey<T>): T? =
  currentCoroutineContext()[key]?.content


class CoroutineElement<T : CoroutineScoped>(
  val content: T,
  override val key: CoroutineKey<T> = CoroutineKey<T>(content)
) : CoroutineContext.Element

fun <T : CoroutineScoped> CoroutineKey<T>.scopedContext(t: T): CoroutineElement<T> =
  CoroutineElement(t, this)