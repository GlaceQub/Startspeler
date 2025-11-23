plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-auth:2.3.4")
    implementation("io.ktor:ktor-server-cors:2.3.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // MySQL JDBC driver (required to connect to a MySQL database)
    implementation("mysql:mysql-connector-java:8.0.33")

    // Connection pool (recommended in production; keeps code simple for POC)
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Optional: Kotlin-friendly SQL library (Exposed). Uncomment if you want an ORM-like DSL.
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")

    // Password hashing (bcrypt) - use to verify stored password hashes securely
    implementation("at.favre.lib:bcrypt:0.9.0")

    // dotenv loader to reuse your existing .env file (e.g., the docker-compose .env)
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
}

application {
    mainClass.set("com.example.server.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}
