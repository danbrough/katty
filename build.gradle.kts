plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.publish) apply false
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.shadow) apply false
}



group = project.property("project.group")!!.toString()
version = project.property("project.version")!!.toString()

subprojects {
  group = rootProject.group
  version = rootProject.version
}

