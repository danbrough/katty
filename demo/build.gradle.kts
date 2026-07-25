import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  applyDefaultHierarchyTemplate()
  jvm()
  linuxX64()
  linuxArm64()

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