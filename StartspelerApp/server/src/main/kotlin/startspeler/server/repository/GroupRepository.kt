package startspeler.server.repository

import com.startspeler.models.Group
import db.tables.Group as GroupTable
import db.tables.User as UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction

class GroupAlreadyExistsException(message: String) : RuntimeException(message)

object GroupRepository {

    fun getAll(): List<Group> = transaction {
        GroupTable.selectAll().orderBy(GroupTable.name, SortOrder.ASC).map { row ->
            Group(
                id = row[GroupTable.id],
                name = row[GroupTable.name],
                discount = row[GroupTable.discount]
            )
        }
    }

    fun getById(id: Int): Group? = transaction {
        GroupTable.select { GroupTable.id eq id }.map { row ->
            Group(
                id = row[GroupTable.id],
                name = row[GroupTable.name],
                discount = row[GroupTable.discount]
            )
        }.singleOrNull()
    }

    fun create(name: String, discount: Float?): Group = transaction {
        if (existsByName(name)) throw GroupAlreadyExistsException("Groep '$name' bestaat al.")

        val stmt = GroupTable.insert { row ->
            row[GroupTable.name] = name
            row[GroupTable.discount] = discount
        }
        val newId = stmt[GroupTable.id]
        Group(id = newId, name = name, discount = discount)
    }

    fun update(id: Int, name: String, discount: Float?): Group? = transaction {
        if (existsByName(name, excludeId = id)) throw GroupAlreadyExistsException("Groep '$name' bestaat al.")

        val updated = GroupTable.update({ GroupTable.id eq id }) { row ->
            row[GroupTable.name] = name
            row[GroupTable.discount] = discount
        }
        if (updated == 0) null else getById(id)
    }

    /** The default group ID that users are moved to when their group is deleted. */
    private const val DEFAULT_GROUP_ID = 1

    fun delete(id: Int): Boolean = transaction {
        if (id == DEFAULT_GROUP_ID) return@transaction false // cannot delete the default group
        // Reassign all members of this group to the default group
        UserTable.update({ UserTable.groupId eq id }) { row ->
            row[UserTable.groupId] = DEFAULT_GROUP_ID
        }
        GroupTable.deleteWhere { GroupTable.id eq id } > 0
    }

    /** Returns members (users) for a given group */
    fun getMembersWithCount(id: Int): Pair<Int, List<Pair<Int, String>>> = transaction {
        val members = UserTable.select { UserTable.groupId eq id }
            .map { row -> Pair(row[UserTable.id], row[UserTable.name]) }
        Pair(members.size, members)
    }

    private fun existsByName(name: String, excludeId: Int? = null): Boolean = transaction {
        val cond = if (excludeId == null) {
            GroupTable.name eq name
        } else {
            (GroupTable.name eq name) and (GroupTable.id neq excludeId)
        }
        GroupTable.select { cond }.limit(1).any()
    }
}
