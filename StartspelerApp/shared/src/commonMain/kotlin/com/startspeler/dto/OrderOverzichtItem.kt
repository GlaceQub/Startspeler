package com.startspeler.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderOverzichtItem(
    val id: Int,
    val tableNumber: Int,
    val status: String,
    val statusId: Int,
    val canGoToPreviousStatus: Boolean = false,
    val canGoToNextStatus: Boolean = false,
    val previousStatusLabel: String? = null,
    val nextStatusLabel: String? = null,
    val canEdit: Boolean = true,
    val canDelete: Boolean = true,
    val canCheckout: Boolean = false,
    val totalPrice: Float,
    val priceAfterDiscount: Float? = null,
    val discountPercentage: Float? = null,
    val clientName: String,
    val placedByStaff: Boolean,
    val orderitems: List<OverzichtOrderitem>,
    val createdAt: String? = null // ISO 8601 string, nullable for backward compatibility
)

@Serializable
data class ClientOpenOrdersSummary(
    val clientName: String,
    val orders: List<OrderOverzichtItem>,
    val totalOpenAmount: Float,
    val totalCheckoutableAmount: Float,
    val totalOpenAmountAfterDiscount: Float? = null,
    val totalCheckoutableAmountAfterDiscount: Float? = null,
    val discountPercentage: Float? = null
)

@Serializable
data class OrderStatusTransitionResponse(
    val success: Boolean,
    val order: OrderOverzichtItem? = null,
    val error: String? = null
)

@Serializable
data class BulkCheckoutResponse(
    val success: Boolean,
    val summary: ClientOpenOrdersSummary? = null,
    val updatedOrderIds: List<Int> = emptyList(),
    val error: String? = null
)

@Serializable
data class PlaceOrderRequest(
    val klant: String,
    val tafel: String,
    val items: List<OrderItemRequest>
)

@Serializable
data class UpdateOrderRequest(
    val klant: String,
    val tafel: String,
    val items: List<OrderItemRequest>
)

@Serializable
data class BulkCheckoutRequest(
    val clientName: String
)

@Serializable
data class DeleteOrderResponse(
    val success: Boolean,
    val deletedOrderId: Int? = null,
    val error: String? = null
)
