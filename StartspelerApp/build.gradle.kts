plugins {
    // Declare Kotlin plugins once in the root and don't apply them here to subprojects.
    kotlin("multiplatform") version "2.2.21" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
    kotlin("jvm") version "2.2.21" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
}

// If you reference ':backend', change to ':server' if needed
