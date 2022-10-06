import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    `maven-publish`
    signing
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
            groupId = project.group as String
            artifactId = "hook-utility"
            version = project.version as String

            from(components["java"])
            pom {
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("ayanoyuugiri")
                        name.set("Ayano Yuugiri")
                    }
                    developer {
                        id.set("liulihaocai")
                        name.set("Takanashi Hosh1no")
                        email.set("liulihaocaiqwq@gmail.com")
                    }
                }
            }
        }
    }
}