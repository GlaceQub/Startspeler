package startspeler.server.repository

import db.tables.Group as GroupTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import com.startspeler.models.Group

object GroepRepository {
    fun getAll(): List<Group> = transaction {
        GroupTable.selectAll().map {
            Group(
                id = it[GroupTable.id],
                name = it[GroupTable.name],
                discount = it[GroupTable.discount],
            )
        }
    }

    fun getGroupById(id: Int): Group? = transaction {
        GroupTable.select { GroupTable.id eq id }
            .map {
                Group(
                    id = it[GroupTable.id],
                    name = it[GroupTable.name],
                    discount = it[GroupTable.discount],
                )
            }
            .singleOrNull()
    }
}