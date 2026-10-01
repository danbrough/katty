import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
  applyDefaultHierarchyTemplate()

  jvm {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_17
    }
  }

  linuxX64()
  linuxArm64()
  /*androidNativeX64()
  androidNativeArm64()*/

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  js {
    nodejs()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    nodejs()
  }
}