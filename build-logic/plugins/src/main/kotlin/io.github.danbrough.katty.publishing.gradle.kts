import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar

plugins {
  id("com.vanniktech.maven.publish")
}

configure<PublishingExtension> {
  publishing {
    repositories {
      maven("file:///tmp/maven") {
        name = "temp"
      }
    }
  }
}


private val descriptions = mapOf("core" to "Basic core functionality for the shell")
private val githubURL = "https://github.com/danbrough/katty"

private val signingKey = project.findProperty("signing.key")?.toString()?.replace("\\n", "\n")
private val signingPassword = project.findProperty("signing.password")?.toString()


if (signingKey == null) project.logger.warn("signing.key not found in project properties")


if (signingPassword == null) project.logger.warn("signing.password not found in project properties")

configure<MavenPublishBaseExtension> {
  // sources publishing is always enabled by the Kotlin Multiplatform plugin
  configure(
    KotlinMultiplatform(
      // whether to publish a sources jar
      sourcesJar = SourcesJar.Sources()
    )
  )

  coordinates(
    groupId = project.group as String,
    artifactId = project.name,
    version = project.version as String
  )

  pom {
    name.set(project.name)
    description.set(descriptions[project.name] ?: error("Missing description for ${project.name}"))
    url.set(githubURL)

    licenses {
      license {
        name.set("MIT License")
        url.set("${githubURL}/blob/main/LICENSE")
      }
    }

    developers {
      developer {
        id.set("danbrough")
        name.set("Dan Brough")
        email.set("dan@danbrough.org")
        url.set("https://danbrough.github.io/")
      }
    }

    scm {
      url.set(githubURL)
      connection.set("scm:git:${githubURL}.git")
      developerConnection.set("scm:git:git@github.com:danbrough/katty.git")
    }
  }

  // Configure publishing to Maven Central
  //publishToMavenCentral()

  if (signingKey != null || signingPassword != null) {
    // Enable GPG signing for all publications
    signAllPublications()
  }
}



if (signingKey != null || signingPassword != null) {
  configure<SigningExtension> {
    extensions.configure<SigningExtension> {
      useInMemoryPgpKeys(signingKey, signingPassword)
    }
  }
}