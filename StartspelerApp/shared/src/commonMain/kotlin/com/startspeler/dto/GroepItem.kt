package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroepItem(
    val id: Int,
    val name: String,
    val discount: Float?,
    val memberCount: Int,
)