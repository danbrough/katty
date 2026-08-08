@file:OptIn(ExperimentalWasmDsl::class, ExperimentalMainFunctionArgumentsDsl::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalMainFunctionArgumentsDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
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

  js {
    nodejs {
      passCliArgumentsToMainFunction()
    }
    binaries.executable()
  }

  sourceSets {
    commonMain {
      dependencies {
        //implementation(projects.clikt)
        implementation(projects.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.io.core)
        implementation(libs.ktoml.core)
        implementation(libs.mordant.markdown)
        implementation(libs.clikt)
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
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  }
}

tasks.withType<NodeJsExec>().configureEach {
  doFirst {
    println("JS FILES: ${this@configureEach.outputs.files.files}")
    println("JS CMD LINE: ${this@configureEach.commandLine}")
    println("JS MAIN: ${this@configureEach.npmProject.main.get()}")
    println("JS EXECUTABLE: ${this@configureEach.executable}")
    println("JS MODULES DIR: ${this@configureEach.npmProject.nodeModulesDir.get()}")
  }
}

tasks.register("getShadowJar") {
  description = "Creates and prints the name of the shadow jar"
  dependsOn("shadowJar")
  val shadowFile = tasks["shadowJar"].outputs.files
  val nodeExecutable = tasks.withType<NodeJsExec>().firstOrNull()?.executable

  doFirst {
    println("shadowJar: ${shadowFile.files.first()}")
    println("nodeExecutable: $nodeExecutable")
  }
}


