package entities

import kotlinx.datetime.LocalDateTime

data class UserEntity(
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
