package io.github.danbrough.katty.client

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.curl.Curl
import io.ktor.client.engine.curl.CurlClientEngineConfig

actual fun ktorClientFactory(): HttpClientEngineFactory<*> = Curl
actual fun HttpClientConfig<HttpClientEngineConfig>.clientEngineConfig() {
  engine {
    if (this is CurlClientEngineConfig) {
      logClient.debug { "setting ca info for CurlClientEngineConfig" }
      caInfo = "/etc/ssl/certs/ca-certificates.crt"
    }
  }
}

