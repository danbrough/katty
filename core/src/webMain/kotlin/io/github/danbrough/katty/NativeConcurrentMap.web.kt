package io.github.danbrough.katty


actual class NativeConcurrentMap<K, V> actual constructor() {
  private val delegate = mutableMapOf<K, V>()

  actual fun put(key: K, value: V) {
    delegate[key] = value
  }

  actual operator fun get(key: K): V? = delegate[key]

  actual fun remove(key: K) {
    delegate.remove(key)
  }

  actual fun toMap(): Map<K, V> = delegate.toMap()

  actual fun clear(): Unit = delegate.clear()
}

