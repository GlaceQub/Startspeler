package com.example.auth

import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

actual class TokenStorage {
    private val key = "kmp_pwa_auth_tokens"
    private val json = Json { ignoreUnknownKeys = true }

    actual suspend fun save(tokens: AuthTokens) = withContext(Dispatchers.Default) {
        window.localStorage.setItem(key, json.encodeToString(tokens))
    }

    actual suspend fun load(): AuthTokens? = withContext(Dispatchers.Default) {
        val s = window.localStorage.getItem(key) ?: return@withContext null
        return@withContext try { json.decodeFromString<AuthTokens>(s) } catch (_: Throwable) { null }
    }

    actual suspend fun clear() = withContext(Dispatchers.Default) {
        window.localStorage.removeItem(key)
    }
}
