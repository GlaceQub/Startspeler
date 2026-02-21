package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderOverzichtItem(
    val id: Int,
    val tableNumber: Int,
    val status: String,
    val totalPrice: Float,
    val clientName: String,
    val placedByStaff: Boolean,
    val orderitems: List<OverzichtOrderitem>
)
