package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductItem(
    val id: Int,
    val name: String,
    val price: Float,
    val outOfStock: Boolean
)

