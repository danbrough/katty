package io.github.danbrough.katty

// If Kotlin 2.1+: import kotlin.concurrent.atomics.AtomicReference
// If older Kotlin: import kotlin.concurrent.AtomicReference
import kotlin.concurrent.atomics.AtomicReference

actual class NativeConcurrentMap<K, V> {
  private val mapRef = AtomicReference<Map<K, V>>(mapOf())

  actual fun put(key: K, value: V) {
    while (true) {
      val current = mapRef.load()
      val updated = current + (key to value)
      if (mapRef.compareAndSet(current, updated)) break
    }
  }

  actual operator fun get(key: K): V? {
    return mapRef.load()[key]
  }

  actual fun remove(key: K) {
    while (true) {
      val current = mapRef.load()
      if (!current.containsKey(key)) break
      val updated = current - key
      if (mapRef.compareAndSet(current, updated)) break
    }
  }

  actual fun toMap(): Map<K, V> {
    return mapRef.load()
  }

  actual fun clear() {
    while (true) {
      val current = mapRef.load()
      if (current.isEmpty()) break
      if (mapRef.compareAndSet(current, mapOf())) break
    }
  }
}
