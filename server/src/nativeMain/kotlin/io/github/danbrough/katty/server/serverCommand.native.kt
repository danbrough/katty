package io.github.danbrough.mv4k.server

import io.ktor.server.application.Application
import org.danbrough.klog.logger


private val log = logger("MV4K_SERVER")


actual suspend fun Application.platformModule(serverContext: ServerContext) {
  serverModule(serverContext)
}