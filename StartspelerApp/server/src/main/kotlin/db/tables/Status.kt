package db.tables

import org.jetbrains.exposed.sql.Table

object Status : Table("Status") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id)
}