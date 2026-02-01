package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class groepToevoegen(
    var name: String = "",
    var discount: Float? = null
)