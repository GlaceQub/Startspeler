package entities

import kotlinx.datetime.LocalDateTime

data class InventoryEntity(
    val id: Int,
    val productId: Int,
    val quantity: Int,
    val minimumQuantity: Int?,
    val lastUpdated: LocalDateTime?
)
