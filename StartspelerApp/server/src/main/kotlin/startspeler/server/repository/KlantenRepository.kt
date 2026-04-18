package startspeler.server.repository

import db.tables.Role
import db.tables.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class KlantNotFoundException(message: String) : RuntimeException(message)
class KlantNameAlreadyExistsException(message: String) : RuntimeException(message)
class KlantEmailAlreadyExistsException(message: String) : RuntimeException(message)

data class KlantModel(
    val id: Int,
    val name: String,
    val email: String?,
    val groupId: Int,
    val roleId: Int,
    val statusId: Int
)

object KlantenRepository {

    fun getAll(nameFilter: String? = null, emailFilter: String? = null): List<KlantModel> = transaction {
        val base = (Role.name eq "klant") and (User.statusId eq 1)

        val cond = when {
            !nameFilter.isNullOrBlank() && !emailFilter.isNullOrBlank() ->
                base and (User.name like "%$nameFilter%") and (User.email like "%$emailFilter%")

            !nameFilter.isNullOrBlank() ->
                base and (User.name like "%$nameFilter%")

            !emailFilter.isNullOrBlank() ->
                base and (User.email like "%$emailFilter%")

            else -> base
        }

        (User innerJoin Role).select { cond }.map(::rowToKlant)
    }

    fun getById(id: Int): KlantModel? = transaction {
        (User innerJoin Role)
            .select { (User.id eq id) and (Role.name eq "klant") }
            .singleOrNull()
            ?.let(::rowToKlant)
    }

    fun nameExists(name: String, excludeId: Int? = null): Boolean = transaction {
        val base = User.name eq name
        val cond = if (excludeId == null) base else base and (User.id neq excludeId)
        User.select { cond }.limit(1).any()
    }

    fun emailExists(email: String, excludeId: Int? = null): Boolean = transaction {
        val base = User.email eq email
        val cond = if (excludeId == null) base else base and (User.id neq excludeId)
        User.select { cond }.limit(1).any()
    }

    fun update(id: Int, name: String, email: String?, groupId: Int?): KlantModel {
        return transaction {
            val existing = getById(id) ?: throw KlantNotFoundException("Geen klant gevonden")

            if (name.isBlank()) throw IllegalArgumentException("Naam is verplicht")
            if (nameExists(name, excludeId = id)) throw KlantNameAlreadyExistsException("Naam van klant is al geregistreerd")
            if (!email.isNullOrBlank() && emailExists(email, excludeId = id)) {
                throw KlantEmailAlreadyExistsException("E-mailadres is al geregistreerd")
            }

            User.update({ User.id eq id }) { row ->
                row[User.name] = name
                row[User.email] = email
                if (groupId != null) row[User.groupId] = groupId
            }

            getById(id) ?: existing
        }
    }

    fun delete(id: Int): Boolean = transaction {
        val existing = getById(id) ?: return@transaction false
        User.deleteWhere { User.id eq existing.id } > 0
    }

    private fun rowToKlant(row: ResultRow): KlantModel =
        KlantModel(
            id = row[User.id],
            name = row[User.name],
            email = row[User.email],
            groupId = row[User.groupId],
            roleId = row[User.roleId],
            statusId = row[User.statusId]
        )
}