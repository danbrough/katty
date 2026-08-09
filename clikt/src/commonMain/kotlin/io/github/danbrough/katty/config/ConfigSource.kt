package io.github.danbrough.katty.config

interface ConfigSource {

  fun hasProperty(name: String): Boolean
  fun getProperty(name: String): String?
  operator fun get(name: String): String? = getProperty(name)
}

interface MutableConfigSource : ConfigSource {
  fun setProperty(name: String, value: String)
  operator fun set(name: String, value: String) = setProperty(name, value)
}

private open class ChainedConfigSource(
  private val configSource: ConfigSource,
  private val delegate: ConfigSource
) :
  ConfigSource {
  override fun hasProperty(name: String): Boolean =
    configSource.hasProperty(name) || delegate.hasProperty(name)

  override fun getProperty(name: String): String? =
    configSource.getProperty(name) ?: delegate.getProperty(name)
}

fun ConfigSource.chain(delegate: ConfigSource): ConfigSource = ChainedConfigSource(this, delegate)

fun MutableConfigSource.chain(delegate: ConfigSource): MutableConfigSource =
  object : ChainedConfigSource(this, delegate), MutableConfigSource {
    override fun setProperty(name: String, value: String) {
      this@chain.setProperty(name, value)
    }
  }

fun ConfigSource.chain(delegate: MutableConfigSource): MutableConfigSource =
  object : ChainedConfigSource(this, delegate), MutableConfigSource {
    override fun setProperty(name: String, value: String) {
      delegate.setProperty(name, value)
    }
  }