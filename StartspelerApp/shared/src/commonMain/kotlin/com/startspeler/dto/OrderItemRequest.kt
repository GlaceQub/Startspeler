package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequest(
    val productId: Int,
    val quantity: Int,
    val price: Float
)
