import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.pubhish) apply false
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.shadow) apply false
}



/*

allprojects {
  afterEvaluate {
    extensions.findByType<KotlinMultiplatformExtension>()?.apply {
    }
  }
}
*/
