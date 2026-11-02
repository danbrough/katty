package io.github.danbrough.katty

expect class NativeConcurrentMap<K, V>() {
  fun put(key: K, value: V)
  operator fun get(key: K): V?
  fun remove(key: K)
  fun toMap(): Map<K, V>
  fun clear()
}