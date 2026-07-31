import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
  `kotlin-dsl`
}

dependencies {
  compileOnly(libs.gradle.kotlin.plugin)
  compileOnly(libs.gradle.publish.plugin)
  compileOnly(libs.gradle.dokka.plugin)
}
