@file:Suppress("UnstableApiUsage")


rootProject.name = "katty"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    google()
    mavenCentral()
    gradlePluginPortal()

  }
}

dependencyResolutionManagement {
  repositories {
    maven("https://maven.danbrough.org")
    google()
    mavenCentral()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.6"
}

include(":core",":demo",":clikt")




