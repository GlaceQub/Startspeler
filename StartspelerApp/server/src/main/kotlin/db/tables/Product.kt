package db.tables

import org.jetbrains.exposed.sql.Table

object Product : Table("Product") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val categoryId = integer("categoryId").references(Category.id)
    val price = float("price")
    val popularity = integer("popularity").nullable()
    override val primaryKey = PrimaryKey(id)
}