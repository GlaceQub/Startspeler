package db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Order : Table("Order") {
    val id = integer("id").autoIncrement()
    val userId = integer("UserId").references(User.id)
    val tableId = integer("tableId").references(TableModel.id)
    val statusId = integer("StatusId").references(Status.id)
    val totalPrice = float("totalPrice")
    val priceAfterDiscount = float("priceAfterDiscount").nullable()
    val createdAt = datetime("createdAt")
    val isPlacedByStaff = bool("isPlacedByStaff")
    val remarks = varchar("remarks", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}