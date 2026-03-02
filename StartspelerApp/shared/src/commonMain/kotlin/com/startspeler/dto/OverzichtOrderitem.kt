package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class OverzichtOrderitem(
    val id: Int,
    val product: String,
    val quantity: Int,
    val price: Float
)