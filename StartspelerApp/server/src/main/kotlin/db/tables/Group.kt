package db.tables

import org.jetbrains.exposed.sql.Table

object Group : Table("Group") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val discount = float("discount").nullable()
    override val primaryKey = PrimaryKey(id)
}