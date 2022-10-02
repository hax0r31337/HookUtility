import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    `maven-publish`
}

group = "me.yuugiri.hutil"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm-tree:9.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "me.yuugiri"
      artifactId = "hutil"
      version = "1.0.0"

      from(components["java"])
    }
  }
}
