plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version "1.8.10"
    id("maven-publish")
}

android {
    namespace = "com.deathsdoor.request-utilities"
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}

object Ktor {
    private const val base = "io.ktor"
    private const val version = "2.0.0"
    const val android = "$base:ktor-client-okhttp:$version"
    const val ios = "$base:ktor-client-ios:$version"
    const val js = "$base:ktor-client-js:$version"
    const val jvm = "$base:ktor-client-cio:$version"
}
publishing {
    val repoName = "AstroPlayer"
    val groupName = "com.deathsdoor.astroplayer"
    val currentVersion = "0.0.6"
    val repoURL = "https://github.com/Deaths-Door/AstroPlayer"
    repositories {
        maven {
            url = uri(repoURL)
            name = repoName
            group = groupName
            version = currentVersion
        }
    }
    publications {
        register("maven", MavenPublication::class) {
            //  from(components["common"])
            groupId = groupName
            artifactId = "astroplayer-core"
            version = currentVersion
            pom {
                name.set(repoName)
                description.set("AstroPlayer is an open-source media player designed for the Kotlin Multiplatform Mobile (KMM) framework. It provides a simple API for audio playback and supports multiple media formats.")
                url.set(repoURL)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}
kotlin {
    android()
    jvm()
    js(IR){
        browser()
        nodejs()
        binaries.executable()
    }

    ios()
    iosX64()
    iosArm64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib"))
                //TODO change to implementation
                api("io.ktor:ktor-client-core:2.2.4")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            }
        }

        val androidMain by getting {
            dependencies{
                implementation(Ktor.android)
                //implementation("io.ktor:ktor-client-okhttp:2.2.4")
            }
        }

        val jvmMain by getting {
            dependencies{
                implementation(Ktor.jvm)
             //   implementation("io.ktor:ktor-client-cio:2.0.0")
            }
        }

        val iosMain by getting {
            dependencies{
                implementation(Ktor.ios)
                //implementation("io.ktor:ktor-client-ios:2.0.0")
            }
        }

        val jsMain by getting {
            dependencies{
                implementation(Ktor.js)
               // implementation("io.ktor:ktor-client-js:2.2.4")
            }
        }
    }
}