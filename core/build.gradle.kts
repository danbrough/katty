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
        implementation(libs.mordant)
        implementation(libs.kotlinx.io.core)
      }
    }
  }
}