package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroepItem(
    val id: String,
    val name: String,
    val discount: Float?,
    val memberCount: Int,
)
