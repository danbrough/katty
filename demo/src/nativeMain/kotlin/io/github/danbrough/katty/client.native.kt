package io.github.danbrough.katty

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.curl.Curl
import io.ktor.client.engine.curl.CurlClientEngineConfig

actual fun ktorClientFactory(): HttpClientEngineFactory<*> = Curl
actual fun HttpClientConfig<HttpClientEngineConfig>.clientConfig() {
  engine {
    if (this is CurlClientEngineConfig){
      logDemo.warn { "setting ca info" }
      caInfo = "/etc/ssl/certs/ca-certificates.crt"
    }
  }
}