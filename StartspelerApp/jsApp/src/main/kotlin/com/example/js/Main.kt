package com.example.js

import com.example.auth.SupabaseAuthRepository
import com.example.auth.TokenStorage
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private val scope = MainScope()

fun main() {
    document.getElementById("root")?.innerHTML = """
        <h1>KMP PWA Starter</h1>
        <input type="email" id="email" placeholder="Email" />
        <input type="password" id="password" placeholder="Password" />
        <button id="signin">Sign In</button>
        <pre id="result"></pre>
    """.trimIndent()

    val supabaseUrl = js("process.env.SUPABASE_URL") as? String ?: "https://your-project.supabase.co"
    val anonKey = js("process.env.SUPABASE_ANON_KEY") as? String ?: "public-anon-key"

    val repo = SupabaseAuthRepository(supabaseUrl, anonKey, TokenStorage())

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
