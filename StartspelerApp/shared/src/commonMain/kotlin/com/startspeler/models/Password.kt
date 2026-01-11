package com.startspeler.models

import kotlinx.datetime.LocalDateTime

@kotlinx.serialization.Serializable
data class Password(
    val id: Int,
    val userId: Int,
    val passwordHash: String,
    val salt: String,
    val lastChanged: LocalDateTime?
)

