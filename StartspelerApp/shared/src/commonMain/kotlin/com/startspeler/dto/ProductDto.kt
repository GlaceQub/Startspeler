package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val price: Float,
    val popularity: Int
)

@Serializable
data class ProductCreateUpdateDto(
    val name: String,
    val categoryId: Int,
    val price: Float,
    val popularity: Int = 0
)

@Serializable
data class ProductMinDto(
    val id: Int,
    val name: String? = null,
    val categoryId: Int? = null
)