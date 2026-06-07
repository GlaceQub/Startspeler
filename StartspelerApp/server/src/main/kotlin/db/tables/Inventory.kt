package db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Inventory : Table("Inventory") {
    val id = integer("id").autoIncrement()
    val productId = integer("productId").references(Product.id)
    val quantity = integer("quantity")
    val minimumQuantity = integer("minimumQuantity").nullable()
    val lastUpdated = datetime("lastUpdated")
    override val primaryKey = PrimaryKey(id)
}