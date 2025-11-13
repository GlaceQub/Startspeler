import java.util.Properties
import java.io.FileInputStream

val versions = Properties().apply {
    load(FileInputStream("${projectDir}/versions.properties"))
}
val kotlinReactVersion = versions["kotlinReactVersion"] as String
val kotlinReactDomVersion = versions["kotlinReactDomVersion"] as String
val csstypeVersion = versions["csstypeVersion"] as String
val kotlinxSerializationVersion = versions["kotlinxSerializationVersion"] as String
val kotlinxCoroutinesVersion = versions["kotlinxCoroutinesVersion"] as String
val muiNpmVersion = versions["muiNpmVersion"] as String
val muiIconsNpmVersion = versions["muiIconsNpmVersion"] as String
val emotionReactNpmVersion = versions["emotionReactNpmVersion"] as String
val emotionStyledNpmVersion = versions["emotionStyledNpmVersion"] as String
val kotlinMuiVersion = versions["kotlinMuiVersion"] as String
val kotlinMuiIconsVersion = versions["kotlinMuiIconsVersion"] as String
val reactNpmVersion = versions["reactNpmVersion"] as String
val reactDomNpmVersion = versions["reactDomNpmVersion"] as String

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
        }
        compilations["main"].defaultSourceSet {
            kotlin.srcDir("src/main/kotlin/com/startspeler/js")
            kotlin.srcDir("src/main/kotlin/com/startspeler/ui")
            resources.srcDir("src/main/resources")
        }
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")

                // Use kotlin-wrappers version catalog for wrappers
                implementation(kotlinWrappers.react)
                implementation(kotlinWrappers.reactDom)
                implementation(kotlinWrappers.emotion.styled)
                implementation(kotlinWrappers.mui.material)
                implementation(kotlinWrappers.mui.iconsMaterial)

                // NPM JS dependencies (MUI + emotion)
                implementation(npm("@mui/material", muiNpmVersion))
                implementation(npm("@mui/icons-material", muiIconsNpmVersion))
                implementation(npm("@emotion/react", emotionReactNpmVersion))
                implementation(npm("@emotion/styled", emotionStyledNpmVersion))
                implementation(npm("react", reactNpmVersion))
                implementation(npm("react-dom", reactDomNpmVersion))
            }
        }
    }
}
