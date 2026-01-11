package com.startspeler.models

@kotlinx.serialization.Serializable
data class Product(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val price: Float,
    val popularity: Int?
)

