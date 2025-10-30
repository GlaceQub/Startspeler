package com.example.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient {
    val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    return HttpClient(Js) {
        install(ContentNegotiation) { json(json) }
    }
}
