import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.serialization")
 // id("org.jetbrains.kotlinx.atomicfu")
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

  compilerOptions {
    optIn.add("kotlin.concurrent.atomics.ExperimentalAtomicApi")
  }
}


tasks.register("thang") {
  description = "thang test"

  // 1. Create a truly local variable inside the configuration block scope
  val localWebSupport = propertyExt.webEnabled.get()

  doLast {
    // 2. Only reference the local variable here
    println("WEB SUPPORT: $localWebSupport")
  }
}

// 1. Define the extension interface
abstract class PropertyExtension {
  abstract val webEnabled: Property<Boolean>

  fun ifWebEnabled(block: () -> Unit) {
    if (webEnabled.get()) block()
  }
}

// 2. Create and register the extension for your projects
val propertyExt = extensions.create<PropertyExtension>("propertyExt")

// 3. Provide the convention value derived from gradle.properties
propertyExt.webEnabled.convention(
  providers.gradleProperty("${project.name}.web.enabled")
    .map { it.toBoolean() }
    .getOrElse(false)
)