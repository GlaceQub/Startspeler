package startspeler.server.repository

import auth.PasswordManager
import db.tables.Role
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object AuthRepository {
    fun authenticate(username: String, password: String): Boolean {
        val user = UserRepository.getUserByName(username) ?: return false
        return PasswordManager.verifyPasswordWithSalt(password, user.salt, user.passwordHash)
    }

    fun getRoleName(username: String): String? = transaction {
        val user = UserRepository.getUserByName(username) ?: return@transaction null
        Role.select { Role.id eq user.roleId }.singleOrNull()?.get(Role.name)
    }
}
