package io.github.danbrough.katty

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO


actual fun ktorClientFactory(): HttpClientEngineFactory<*> = CIO
actual fun HttpClientConfig<HttpClientEngineConfig>.clientConfig() {
}