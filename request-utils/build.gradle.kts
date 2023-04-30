plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version "1.8.10"
    id("maven-publish")
    kotlin("native.cocoapods")
}

android {
    namespace = "com.deathsdoor.request_utils"
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

kotlin {
    android {
        publishLibraryVariants("release")
    }
    
    jvm()
    js(IR){
        browser()
        nodejs()
        binaries.executable()
    }

    ios()
    
    cocoapods {
        summary = "Request-Utilities is a Kotlin Multiplatform library that provides a simple and efficient way to make HTTP requests in Kotlin Multiplatform projects. Request-Utilities is designed to be easy to use, customizable, and extendable."
        version = "0.1.1"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "request-utils"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
                implementation("io.ktor:ktor-client-core:2.2.4")
            }
        }

        val androidMain by getting {
            dependencies{
                implementation(Ktor.android)
            }
        }

        val jvmMain by getting {
            dependencies{
                implementation(Ktor.jvm)
            }
        }

        val iosMain by getting {
            dependencies{
                implementation(Ktor.ios)
            }
        }

        val jsMain by getting {
            dependencies{
                implementation(Ktor.js)
            }
        }
    }
}
