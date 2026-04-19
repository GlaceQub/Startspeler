package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto (
    val id: Int,
    val name: String,
    val discountPercentage: Float? = null
)

