import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlin.multiplatform)

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


  sourceSets {
    commonMain {
      dependencies {
        api(libs.mordant)
        api(libs.kotlinx.io.core)
      }
    }
  }


}