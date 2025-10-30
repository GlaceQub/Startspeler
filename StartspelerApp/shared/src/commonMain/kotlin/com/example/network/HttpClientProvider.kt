package com.example.network

import io.ktor.client.*

/**
 * Expect/actual provider for Ktor HttpClient so shared code can be agnostic to platform engine.
 */
expect fun createHttpClient(): HttpClient
