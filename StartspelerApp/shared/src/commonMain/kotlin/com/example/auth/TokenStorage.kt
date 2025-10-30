package com.example.auth

/**
 * Expect/actual abstraction for token storage.
 * Implement the actual on each target (JS uses localStorage here).
 */
expect class TokenStorage() {
    suspend fun save(tokens: AuthTokens)
    suspend fun load(): AuthTokens?
    suspend fun clear()
}
