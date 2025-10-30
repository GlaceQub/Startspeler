plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig { cssSupport.enabled = true }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:${property("ktor.version")}")
                implementation("io.ktor:ktor-client-content-negotiation:${property("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${property("ktor.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("kotlinx.serialization.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinx.coroutines.version")}")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:${property("ktor.version")}")
            }
        }
    }
}
