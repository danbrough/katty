plugins {
  id("io.github.danbrough.katty.kmp")
  id("io.github.danbrough.katty.dokka")
  id("io.github.danbrough.katty.publishing")
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