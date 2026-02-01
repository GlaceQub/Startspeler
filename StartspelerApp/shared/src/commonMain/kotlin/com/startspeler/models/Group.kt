package com.startspeler.models

@kotlinx.serialization.Serializable
data class Group(
    val id: Int,
    val name: String,
    val discount: Float?,
    val memberCount: Int,
)

