package startspeler.server.repository


import com.startspeler.dto.GroepItem
import db.tables.Group
import db.tables.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object GroepRepository{

    //GET ALL GROUPS (simpler, more robust)
    fun getAll(): List<GroepItem> = transaction {
        Group.selectAll().map { row ->
            val gid = row[Group.id]
            val memberCount = User.select { User.groupId eq gid }.count().toInt()
            GroepItem(
                id = gid,
                name = row[Group.name],
                discount = row[Group.discount],
                memberCount = memberCount
            )
        }
    }

    //GET GROUP BY ID
    fun getGroupById(id: Int): GroepItem? = transaction {
        val row = Group.select { Group.id eq id }.firstOrNull() ?: return@transaction null
        val memberCount = User.select { User.groupId eq id }.count().toInt()
        GroepItem(
            id = row[Group.id],
            name = row[Group.name],
            discount = row[Group.discount],
            memberCount = memberCount
        )
    }

    //ADD GROUP
    fun addGroup(name: String, discount: Float?): GroepItem = transaction {
        val newId = Group.insert {
            it[Group.name] = name
            it[Group.discount] = discount
        } get Group.id


        GroepItem(
            id = newId,
            name = name,
            discount = discount,
            memberCount = 0
        )
    }

    //DELETE GROUP
    fun deleteGroup(id: Int): Boolean = transaction {
        Group.deleteWhere { Group.id eq id } > 0
    }

}