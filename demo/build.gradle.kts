import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.shadow)
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
        implementation(projects.clikt)
      }
    }
  }

  targets.withType<KotlinNativeTarget>().configureEach {
    binaries {
      executable("katty") {
        entryPoint = "io.github.danbrough.katty.main"
        if (buildType == NativeBuildType.DEBUG && konanTarget == KonanTarget.LINUX_X64) linkerOpts += "--allow-multiple-definition"
      }
    }
  }

  tasks.withType<ShadowJar> {
    mainClass = "io.github.danbrough.katty.JvmMain"
  }
}


tasks.register("getShadowJar") {
  description = "Creates and prints the name of the shadow jar"
  dependsOn("shadowJar")
  val shadowFile = tasks["shadowJar"].outputs.files
  doFirst {
    println("shadowJar: ${shadowFile.files.first()}")
  }
}