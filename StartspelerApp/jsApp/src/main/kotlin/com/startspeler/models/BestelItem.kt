package com.startspeler.models

data class BestelItem(
    val id: String,
    val name: String,
    val price: String,
    val outOfStock: Boolean = false
)
