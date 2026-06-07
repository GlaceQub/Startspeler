package db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Password : Table("Password") {
    val id = integer("id").autoIncrement()
    val userId = integer("UserId").uniqueIndex().references(User.id)
    val passwordHash = varchar("passwordHash", 255)
    val salt = varchar("salt", 255)
    val lastChanged = datetime("lastChanged").nullable()
    override val primaryKey = PrimaryKey(id)
}