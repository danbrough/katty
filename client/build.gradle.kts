plugins {
  id("io.github.danbrough.katty.kmp")
  id("io.github.danbrough.katty.dokka")
  id("io.github.danbrough.katty.publishing")
}


kotlin {


  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.klog)
        api(projects.core)
        implementation(libs.kotlinx.io.core)
        implementation(libs.ktor.network)
        implementation(libs.ktor.io)
        implementation(libs.ktor.serialization.kotlinx.json)
        api(libs.ktor.client.websockets)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.auth)
        implementation(libs.ktor.http)
        //implementation(libs.kotlincrypto.md)
        implementation(libs.kotlincrypto.sha2)

      }
    }

    jvmMain {
      dependencies {
        implementation(libs.ktor.client.cio)
        implementation(libs.logback.classic)
      }
    }

    nativeMain {
      dependencies {
        implementation(libs.ktor.client.curl)
      }
    }

    webMain {
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
  }
}







