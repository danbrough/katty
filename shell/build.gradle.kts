@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  id("io.github.danbrough.katty.kmp")
  id("io.github.danbrough.katty.dokka")
  id("io.github.danbrough.katty.publishing")
}


kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core)
        api(libs.mordant)
        api(libs.mordant.coroutines)
        api(libs.kotlinx.io.core)
        api(libs.kotlinx.coroutines.core)
        //implementation(libs.ktoml.core)
        implementation(libs.klog)
      }
    }
  }
}
