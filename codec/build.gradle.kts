plugins {
  id("io.github.danbrough.katty.kmp")
}


kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core)
        implementation(libs.korlibs.compression)
        implementation(libs.kotlinx.serialization.core)
      }
    }
  }
}