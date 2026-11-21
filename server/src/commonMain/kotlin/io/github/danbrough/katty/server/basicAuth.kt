package io.github.danbrough.katty.server

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserHashedTableAuth
import io.ktor.server.auth.basic
import org.kotlincrypto.hash.sha2.SHA256

val sha256DigestFunction: (String) -> ByteArray = {
  SHA256().digest(it.encodeToByteArray())
}

const val DEFAULT_BASIC_AUTH_NAME = "auth-basic"
const val DEFAULT_REALM_NAME = "Private Zone"

fun Application.kattyBasicAuth(
  authName: String = DEFAULT_BASIC_AUTH_NAME,
  realmName: String = DEFAULT_REALM_NAME,
  digestFunction: (String) -> ByteArray = sha256DigestFunction,
  users: Map<String, String>,
) {

  val hashedUserTable = UserHashedTableAuth(
    table = users.mapValues { digestFunction(it.value) },
    digester = digestFunction
  )

  install(Authentication) {
    basic(authName) {
      realm = realmName
      validate { credentials ->
        hashedUserTable.authenticate(credentials)
      }
    }
  }
}