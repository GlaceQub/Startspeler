import java.util.Properties
import java.io.FileInputStream
import java.io.File

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

fun parseDotEnv(file: File): Map<String, String> {
    if (!file.exists()) return emptyMap()

    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains('=') }
        .associate { line ->
            val separator = line.indexOf('=')
            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim().removeSurrounding("\"")
            key to value
        }
}

val envCandidates = listOf(
    rootDir.resolve(".env"),
    rootDir.resolve("server/.env"),
    rootDir.parentFile.resolve("Database/docker/.env")
)

val envValues = buildMap<String, String> {
    envCandidates.forEach { candidate ->
        putAll(parseDotEnv(candidate))
    }
}

val configuredBackendUrl =
    providers.gradleProperty("backendUrl").orNull
        ?: System.getenv("BACKEND_URL")
        ?: envValues["BACKEND_URL"]
        ?: "http://localhost:8080"

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
            kotlin.srcDir("src/main/kotlin")
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

tasks.named<Copy>("jsProcessResources") {
    exclude("public/config.json")

    from("src/main/resources/public") {
        exclude("config.json")
        into("")
    }

    doFirst {
        val generatedConfigDir = layout.buildDirectory.dir("generated/js-config").get().asFile
        generatedConfigDir.mkdirs()
        File(generatedConfigDir, "config.json").writeText(
            """
            {
              "backendUrl": "$configuredBackendUrl"
            }
            """.trimIndent()
        )
    }

    from(layout.buildDirectory.dir("generated/js-config")) {
        into("")
    }
}
