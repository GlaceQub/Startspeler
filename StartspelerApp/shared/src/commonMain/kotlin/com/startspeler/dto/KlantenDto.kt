package com.startspeler.klanten.dto

import kotlinx.serialization.Serializable

@Serializable
data class KlantenDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val groupId: Int,
    val roleId: Int,
    val statusId: Int
)

@Serializable
data class KlantAddRequestDto(
    val name: String,
    val email: String? = null
)

@Serializable
data class KlantUpdateRequestDto(
    val name: String,
    val email: String? = null,
    val groupId: Int? = null
)