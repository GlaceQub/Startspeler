package com.startspeler.models

@kotlinx.serialization.Serializable
data class OrderItem(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val price: Float
)

