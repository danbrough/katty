plugins {
  id("io.github.danbrough.katty.kmp")
}


kotlin {


  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.klog)
        api(projects.core)
        implementation(libs.ktor.network)
        implementation(libs.ktor.io)
        implementation(libs.ktor.serialization.kotlinx.json)
        api(libs.ktor.server.core)
        api(libs.ktor.server.websockets)
        implementation(libs.ktor.server.content.negotiation)
        implementation(libs.ktor.server.forwarded.header)
        //implementation("io.ktor:ktor-server-forwarded-header:3.6.0")
        implementation(libs.ktor.server.auth)
        implementation(libs.ktor.server.cio)
        implementation(libs.ktor.http)
        //implementation(libs.kotlincrypto.md)
        implementation(libs.kotlincrypto.sha2)
        implementation(libs.kotlinx.io.core)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.logback.classic)
      }
    }

    nativeMain {
      dependencies {
        //implementation(libs.kotlincrypto.hash)
      }
    }
  }
}

