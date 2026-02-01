package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val product: ProductItem,
    val quantity: Int
)