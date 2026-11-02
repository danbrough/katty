package io.github.danbrough.katty

import java.util.concurrent.ConcurrentHashMap

actual class NativeConcurrentMap<K, V> {
  @Suppress("JavaCollectionWithNullableTypeArgument")
  private val delegate = ConcurrentHashMap<K, V>()

  actual fun put(key: K, value: V) {
    delegate[key] = value
  }

  actual operator fun get(key: K): V? = delegate[key]


  actual fun remove(key: K) {
    delegate.remove(key)
  }

  actual fun toMap(): Map<K, V> = delegate.toMap()


  actual fun clear() = delegate.clear()

}
