import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
    signing
}

group = "me.yuugiri.hutil"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm-tree:9.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

fun gitHash(): String {
    val gitFolder = "$rootDir/.git/"
    val takeFromHash = 7
    val head = File(gitFolder, "HEAD").readText().split(":") // .git/HEAD
    if(head.size == 1) return head[0].trim().take(takeFromHash)

    val refHead = File(gitFolder, head[1].trim()) // .git/refs/heads/master
    return refHead.readText().trim().take(takeFromHash)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mccheatz/HookUtility")
            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("GPR_USERNAME") ?: "null").toString()
                password = (project.findProperty("gpr.key") ?: System.getenv("GPR_TOKEN") ?: "null").toString()
            }
        }
    }
    publications {
        register<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = "hook-utility"
            version = gitHash()

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