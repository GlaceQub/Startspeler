package startspeler.server.repository


import com.startspeler.dto.GroepItem
import db.tables.Group
import db.tables.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object GroepRepository{

    //GET ALL GROUPS
    fun getAll(): List<GroepItem> = transaction {
        val membercount = User.id.count()
        (Group leftJoin User)
            .slice(Group.id, Group.name, Group.discount, membercount)
            .selectAll()
            .groupBy(Group.id, Group.name, Group.discount)
            .map { row ->
                GroepItem(
                    id = row[Group.id].toString(),
                    name = row[Group.name],
                    discount = row[Group.discount],
                    memberCount = row[membercount].toInt()
                )
            }
    }

    //GET GROUP BY ID
    fun getGroupById(id: Int): GroepItem? = transaction {
        val membercount = User.id.count()
        (Group leftJoin User)
            .slice(Group.id, Group.name, membercount)
            .select { Group.id eq id }
            .groupBy(Group.id, Group.name)
            .map { row ->
                GroepItem(
                    id = row[Group.id].toString(),
                    name = row[Group.name],
                    discount = row[Group.discount],
                    memberCount = row[membercount].toInt()
                )
            }
            .firstOrNull()
    }

    //ADD GROUP
    fun addGroup(name: String, discount: Float?): GroepItem = transaction {
        val newId = Group.insert {
            it[Group.name] = name
            it[Group.discount] = discount
        } get Group.id


        GroepItem(
            id = newId.toString(),
            name = name,
            discount = null,
            memberCount = 0
        )
    }

    //DELETE GROUP
    fun deleteGroup(id: Int): Boolean = transaction {
        Group.deleteWhere { Group.id eq id } > 0
    }

}


