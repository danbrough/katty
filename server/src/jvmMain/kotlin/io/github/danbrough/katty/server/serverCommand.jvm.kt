package io.github.danbrough.mv4k.server


import io.github.danbrough.katty.KattyUtils
import io.ktor.http.auth.DigestAlgorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.digest
import io.ktor.server.auth.principal
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi
import org.danbrough.klog.logger
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8


private val log = logger("MV4K_SERVER")
actual suspend fun Application.platformModule(serverContext: ServerContext) {
  serverModule(serverContext)
}

@OptIn(ExperimentalKtorApi::class)
fun Application.platformModuleOld(serverContext: ServerContext) {
  // 1. Define an in-memory or database user-hash table
  val realmName = "Access to the '/' path"

  // For demonstration, a map of usernames to their HA1 hashes
  val hashedUserTable = mapOf(
    "admin" to computeHA1(
      "admin",
      realmName,
      KattyUtils.getEnv("MV4K_PASSWORD") ?: error("MV4K_PASSWORD not set")
    )
  )

  // 2. Install and configure Authentication
  install(Authentication) {


    digest("auth-digest") {
      realm = realmName
      // Minimum recommended algorithm for production is SHA-256
      algorithms = listOf(DigestAlgorithm.SHA_256)

      // Provide a lookup function for the requested user
      digestProvider { userName, realm ->
        hashedUserTable[userName]
      }
    }
  }

  routing {
    // Public route
    get("/") {
      call.respondText("Welcome to the public homepage!")
    }

    // Protected routes
    authenticate("auth-digest") {
      get("/secure/dashboard/{path...}") {
        // Safely retrieve the authenticated username
        val principal = call.principal<UserIdPrincipal>()
        call.respondText(
          "Hello, ${principal?.name}! Welcome to the secure dashboard. at ${
            call.pathParameters.getAll(
              "path"
            )?.joinToString("/")
          }"
        )
      }
    }
  }
}

// Helper function to dynamically generate or pre-calculate the HA1 hash
fun computeHA1(username: String, realm: String, secret: String): ByteArray {
  val md = MessageDigest.getInstance("SHA-256")
  return md.digest("$username:$realm:$secret".toByteArray(UTF_8))
}