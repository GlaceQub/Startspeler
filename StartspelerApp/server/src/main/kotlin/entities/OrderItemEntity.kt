package entities

data class OrderItemEntity(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val price: Float
)
