package com.example.auth

import com.example.network.createHttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
private data class TokenResponse(val access_token: String, val refresh_token: String? = null, val expires_in: Long = 0)

class SupabaseAuthRepository(
    private val supabaseUrl: String,
    private val anonKey: String,
    private val storage: TokenStorage
) {
    private val client = createHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun signInWithEmail(email: String, password: String): Result<AuthTokens> = withContext(Dispatchers.Default) {
        try {
            val url = "$supabaseUrl/auth/v1/token?grant_type=password"
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header("apikey", anonKey)
                setBody(mapOf("email" to email, "password" to password))
            }
            val body = response.bodyAsText()
            val parsed = json.decodeFromString<TokenResponse>(body)
            val tokens = AuthTokens(
                accessToken = parsed.access_token,
                refreshToken = parsed.refresh_token,
                expiresAtEpochSeconds = if (parsed.expires_in > 0) (Clock.System.now().toEpochMilliseconds() / 1000) + parsed.expires_in else null
            )
            storage.save(tokens)
            Result.success(tokens)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun signOut() {
        storage.clear()
    }

    suspend fun currentTokens(): AuthTokens? = storage.load()
}
