package com.example.js

import com.example.auth.SupabaseAuthRepository
import com.example.auth.TokenStorage
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val scope = MainScope()

@Serializable
private data class AppConfig(val backendUrl: String, val supabaseUrl: String, val supabaseAnonKey: String)

suspend fun loadConfig(): AppConfig {
    val response = window.fetch("/config.json").await()
    val text = response.text().await()
    return Json.decodeFromString(AppConfig.serializer(), text)
}

fun main() {
    document.getElementById("root")?.innerHTML = """
        <h1>Startspeler</h1>
        <input type="email" id="email" placeholder="Email" />
        <input type="password" id="password" placeholder="Password" />
        <button id="signin">Sign In</button>
        <pre id="result"></pre>
    """.trimIndent()

    scope.launch {
        val config = loadConfig()
        val repo = SupabaseAuthRepository(config.supabaseUrl, config.supabaseAnonKey, TokenStorage())

        document.getElementById("signin")?.addEventListener("click", {
            val email = (document.getElementById("email") as? dynamic)?.value as? String ?: ""
            val password = (document.getElementById("password") as? dynamic)?.value as? String ?: ""
            val resultEl = document.getElementById("result")

            scope.launch {
                resultEl?.textContent = "Signing in..."
                val res = repo.signInWithEmail(email, password)
                if (res.isSuccess) {
                    resultEl?.textContent = "Signed in: accessToken length=${res.getOrNull()?.accessToken?.length}"
                } else {
                    resultEl?.textContent = "Sign-in failed: ${res.exceptionOrNull()?.message}"
                }
            }
        })
    }
}
