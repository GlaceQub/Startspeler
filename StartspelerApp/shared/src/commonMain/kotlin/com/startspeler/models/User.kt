package com.startspeler.models

import kotlinx.datetime.LocalDateTime

@kotlinx.serialization.Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String?,
    val groupId: Int,
    val roleId: Int,
    val statusId: Int,
    val createdAt: LocalDateTime,
    val passwordHash: String,
    val salt: String
)

