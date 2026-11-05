package io.github.danbrough.katty

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConcurrentMap<K, V> {

  private val map = HashMap<K, V>()
  private val mutex = Mutex()

  suspend fun put(key: K, value: V) = mutex.withLock {
    map[key]?.also {
      if (it is AutoCloseable)
        it.close()
    }
    map[key] = value
  }

  suspend operator fun get(key: K): V? = mutex.withLock {
    map[key]
  }

  suspend fun getOrPut(key: K, creator: () -> V): V = mutex.withLock {
    map.getOrPut(key, creator)
  }

  suspend fun remove(key: K): V? = mutex.withLock {
    map.remove(key)?.also {
      if (it is AutoCloseable) it.close()
    }
  }

  fun values(): Iterable<V> = map.values

  fun clear() {
    map.values.forEach {
      if (it is AutoCloseable) it.close()
    }
    map.clear()
  }


}