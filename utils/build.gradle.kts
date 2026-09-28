@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kmp.android.library)
  id("io.github.danbrough.katty.kmp")
  id("io.github.danbrough.katty.dokka")
  id("io.github.danbrough.katty.publishing")
}

kotlin {


  android {
    compileSdk { version = release(37) }
    minSdk = 27
    namespace = "io.github.danbrough.katty.utils"

    packaging {
      jniLibs {
        useLegacyPackaging = true
      }
    }
  }

  sourceSets{
    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.io.core)
      }
    }

    val jvmSharedMain = create("jvmSharedMain") {
      dependsOn(commonMain.get())
    }

    jvmMain{
      dependsOn(jvmSharedMain)
    }

    androidMain{
      dependsOn(jvmSharedMain)
    }
  }
}