package com.startspeler.tables.dto

import kotlinx.serialization.Serializable

@Serializable
data class Table(
    val id: Int,
    val number: Int,
    val statusId: Int,
    val statusName: String
)

@Serializable

data class TafelCreate(
    val number: Int,
    val statusId: Int
)

@Serializable
data class TafelStatusUpdate(
    val statusId: Int
)

