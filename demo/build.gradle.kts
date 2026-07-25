import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  applyDefaultHierarchyTemplate()
  jvm()
  linuxX64()
  linuxArm64()

  if (HostManager.hostIsMac){
    macosX64()
    macosArm64()
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.io.core)
        implementation(projects.core)
      }
    }
  }

  targets.withType<KotlinNativeTarget>().configureEach {
    binaries {
      executable("katty") {
        entryPoint = "io.github.danbrough.katty.main"
      }
    }
  }
}