package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class InventoryDto(
    val id: Int,
    val productId: Int,
    val quantity: Int,
    val minimumQuantity: Int? = null,
    val lastUpdated: String? = null
)


@Serializable
data class InventoryUpdateRequest(
    val quantity: Int,
    val minimumQuantity: Int? = null
)
