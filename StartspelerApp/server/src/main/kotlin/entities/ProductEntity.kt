package entities

data class ProductEntity(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val price: Float,
    val popularity: Int?
)
