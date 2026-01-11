package com.startspeler.models

import kotlinx.datetime.LocalDateTime

@kotlinx.serialization.Serializable
data class Order(
    val id: Int,
    val userId: Int,
    val tableId: Int,
    val statusId: Int,
    val totalPrice: Float,
    val priceAfterDiscount: Float?,
    val createdAt: LocalDateTime,
    val isPlacedByStaff: Boolean,
    val remarks: String?
)

