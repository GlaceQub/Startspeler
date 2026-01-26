package com.startspeler.models

import kotlinx.datetime.LocalDateTime

@kotlinx.serialization.Serializable
data class Inventory(
    val id: Int,
    val productId: Int,
    val quantity: Int,
    val minimumQuantity: Int?,
    val lastUpdated: LocalDateTime?
)

