package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupCreate(
    val name: String,
    val discount: Float? = null
)

@Serializable
data class GroupUpdate(
    val name: String,
    val discount: Float? = null
)

@Serializable
data class GroupMemberItem(
    val id: Int,
    val name: String
)

@Serializable
data class GroupOverviewItem(
    val id: Int,
    val name: String,
    val discountPercentage: Float,
    val memberCount: Int,
    val members: List<GroupMemberItem>
)
