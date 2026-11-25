package io.github.danbrough.katty.client

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import org.danbrough.klog.logger

internal val logClient = logger("KATTY_CLIENT")

expect fun ktorClientFactory(): HttpClientEngineFactory<*>
expect fun HttpClientConfig<HttpClientEngineConfig>.clientEngineConfig()
