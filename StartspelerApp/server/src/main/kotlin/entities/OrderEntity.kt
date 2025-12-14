package entities

import kotlinx.datetime.LocalDateTime

data class OrderEntity(
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
