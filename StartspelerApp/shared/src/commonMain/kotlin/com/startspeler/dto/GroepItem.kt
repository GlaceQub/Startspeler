package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupItem(
    val id: Int,

    val name: String,
)