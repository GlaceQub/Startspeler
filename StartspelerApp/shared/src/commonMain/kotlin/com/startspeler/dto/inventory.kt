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

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String
)

@Serializable
data class ProductMinDto(
    val id: Int,
    val name: String? = null,
    val categoryId: Int? = null
)