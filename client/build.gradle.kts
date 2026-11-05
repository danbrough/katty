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
        implementation(libs.ktor.serialization.kotlinx.cbor)

        api(libs.ktor.client.websockets)
        implementation(libs.ktor.client.content.negotiation)

        //implementation("io.ktor:ktor-server-forwarded-header:3.6.0")
        implementation(libs.ktor.client.auth)
        implementation(libs.ktor.client.cio)
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







