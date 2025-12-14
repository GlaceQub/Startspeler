package db.tables

import org.jetbrains.exposed.sql.Table

object Category : Table("Category") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id)
}