plugins {
    kotlin("multiplatform") version property("kotlin.version").toString() apply false
    kotlin("js") version property("kotlin.version").toString() apply false
    kotlin("plugin.serialization") version property("kotlin.version").toString() apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
}
