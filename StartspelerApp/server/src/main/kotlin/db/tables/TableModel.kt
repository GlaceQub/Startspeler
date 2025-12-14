package db.tables

import org.jetbrains.exposed.sql.Table

object TableModel : Table("Table") {
    val id = integer("id").autoIncrement()
    val number = integer("number")
    val statusId = integer("StatusId").references(Status.id)
    override val primaryKey = PrimaryKey(id)
}