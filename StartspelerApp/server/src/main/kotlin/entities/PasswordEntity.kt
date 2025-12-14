package entities

import kotlinx.datetime.LocalDateTime

data class PasswordEntity(
    val id: Int,
    val userId: Int,
    val passwordHash: String,
    val salt: String,
    val lastChanged: LocalDateTime?
)
