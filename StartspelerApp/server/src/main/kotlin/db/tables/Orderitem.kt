package db.tables

import org.jetbrains.exposed.sql.Table

object Orderitem : Table("Orderitem") {
    val id = integer("id").autoIncrement()
    val orderId = integer("orderId").references(Order.id)
    val productId = integer("productId").references(Product.id)
    val quantity = integer("quantity")
    val price = float("price")
    override val primaryKey = PrimaryKey(id)
}