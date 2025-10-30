plugins {
    kotlin("js")
    kotlin("plugin.serialization") version property("kotlin.version").toString()
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig { cssSupport.enabled = true }
            runTask {
                devServer = devServer?.copy(port = 3000)
            }
        }
        binaries.executable()
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("kotlinx.serialization.version")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinx.coroutines.version")}")
}
