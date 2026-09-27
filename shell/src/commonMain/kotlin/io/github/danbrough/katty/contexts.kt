package io.github.danbrough.katty

import io.github.danbrough.katty.Errors.errorMissingContext
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

interface KattyContext : CoroutineContext.Element, AutoCloseable

interface ShellContext : KattyContext {
  fun add(element: CoroutineContext.Element)
  fun remove(key: CoroutineContext.Key<*>)
}

private object ShellContextKey : CoroutineContext.Key<ShellContext>


@Suppress("UNCHECKED_CAST")
suspend fun <S : ShellContext> shellContext(): S = currentCoroutineContext()[ShellContextKey] as? S
  ?: errorMissingContext<ShellContext>("No shell context found")

operator fun <S : ShellContext> S.plusAssign(item: CoroutineContext.Element) = add(item)

operator fun <S : ShellContext> S.minusAssign(key: CoroutineContext.Key<*>) = remove(key)
