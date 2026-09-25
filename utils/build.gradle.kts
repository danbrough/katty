@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kmp.android.library)
  id("io.github.danbrough.katty.dokka")
  id("io.github.danbrough.katty.publishing")
}

kotlin {
  applyDefaultHierarchyTemplate()

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

  jvm {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_17
    }
  }

  linuxX64()
  linuxArm64()
  androidNativeArm64()
  androidNativeX64()

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  js {
    nodejs()
  }

  wasmJs {
    nodejs()
  }

/*  wasmWasi{
    nodejs()
  }*/

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