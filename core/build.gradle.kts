@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.publish)
}

kotlin {
  applyDefaultHierarchyTemplate()
  jvm()
  linuxX64()
  linuxArm64()

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  js {
    nodejs()
  }

  sourceSets {
    commonMain {
      dependencies {
        api(libs.mordant)
        api(libs.mordant.markdown)

        api(libs.kotlinx.io.core)
        implementation(libs.ktoml.core)
      }
    }
  }


}
