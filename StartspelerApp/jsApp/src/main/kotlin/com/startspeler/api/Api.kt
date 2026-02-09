package com.startspeler.api

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.js.json
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val groupId: Int = 1,
    val roleId: Int = 2,
    val statusId: Int = 1,
    val createdAt: String? = null,
    val passwordHash: String? = null,
    val salt: String? = null
)

@Serializable
data class UpdateUserRequestDto(
    val name: String? = null,
    val email: String? = null,
    val groupId: Int? = null,
    val roleId: Int? = null,
    val statusId: Int? = null,
    val confirmSimilar: Boolean? = null
)

class ApiException(val status: Int, val body: String) : Exception("API error $status: $body")

object Api {
    private val json = Json { ignoreUnknownKeys = true }

    private fun buildInit(method: String, body: String? = null): RequestInit {
        val headers = json("Content-Type" to "application/json")
        return RequestInit(method = method, headers = headers, body = body)
    }

    private suspend fun checkResponse(resp: Response): String {
        val text = resp.text().await()
        if (!resp.ok) throw ApiException(resp.status, text)
        return text
    }

    suspend fun fetchUsers(baseUrl: String, query: String? = null, limit: Int = 50, offset: Int = 0): List<UserDto> {
        val qPart = query?.takeIf { it.isNotBlank() }?.let { "?q=${window.encodeURIComponent(it)}&limit=$limit&offset=$offset" } ?: "?limit=$limit&offset=$offset"
        val url = "$baseUrl/admin/users$qPart"
        val resp = window.fetch(url).await()
        val body = checkResponse(resp)
        return json.decodeFromString(ListSerializer(UserDto.serializer()), body)
    }

    suspend fun getUserById(baseUrl: String, id: Int): UserDto {
        val resp = window.fetch("$baseUrl/admin/users/$id").await()
        val body = checkResponse(resp)
        return json.decodeFromString(UserDto.serializer(), body)
    }

    suspend fun updateUser(baseUrl: String, id: Int, payload: UpdateUserRequestDto): String? {
        val body = json.encodeToString(payload)
        val resp = window.fetch("$baseUrl/admin/users/$id", buildInit("PUT", body)).await()
        return checkResponse(resp)
    }

    suspend fun deleteUser(baseUrl: String, id: Int) {
        val resp = window.fetch("$baseUrl/admin/users/$id", buildInit("DELETE")).await()
        checkResponse(resp)
    }

    suspend fun assignGroup(baseUrl: String, userId: Int, groupId: Int) {
        val body = json.encodeToString(mapOf("groupId" to groupId))
        val resp = window.fetch("$baseUrl/admin/users/$userId/group", buildInit("POST", body)).await()
        checkResponse(resp)
    }
}