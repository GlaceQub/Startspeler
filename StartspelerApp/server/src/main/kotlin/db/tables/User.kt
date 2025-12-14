package db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object User : Table("User") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val email = varchar("email", 100).nullable()
    val groupId = integer("groupId").references(Group.id)
    val roleId = integer("roleId").references(Role.id)
    val statusId = integer("StatusId").references(Status.id)
    val createdAt = datetime("createdAt")
    override val primaryKey = PrimaryKey(id)
}