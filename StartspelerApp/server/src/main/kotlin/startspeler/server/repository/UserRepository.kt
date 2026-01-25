package startspeler.server.repository

import com.startspeler.models.User
import db.tables.Password
import db.tables.User as DbUser
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utils.UtcNow
import auth.PasswordManager
object UserRepository {
    /** Create a default Admin user if none exists. */
    fun createDefaultAdminUser() {
        val defaultAdminUsername = "admin"
        transaction {
            val exists = DbUser.select { DbUser.name eq defaultAdminUsername }.count() > 0
            if (!exists) {
                val defaultAdminPassword = "St4rtsp3l3r"
                val salt = PasswordManager.generateSalt()
                val passwordHash = PasswordManager.hashPasswordWithSalt(defaultAdminPassword, salt)
                // Use default groupId, roleId, statusId = 1
                createUser(defaultAdminUsername, passwordHash, salt, groupId = 1, roleId = 1, statusId = 1)
            }
        }
    }

    /** Create a new user with optional password and salt. */
    fun createUser(
        username: String,
        email: String? = null,
        passwordHash: String? = null,
        salt: String? = null,
        groupId: Int,
        roleId: Int,
        statusId: Int
    ) {
        transaction {
            val userId = DbUser.insert {
                it[name] = username
                it[DbUser.groupId] = groupId
                it[DbUser.roleId] = roleId
                it[DbUser.statusId] = statusId
                it[DbUser.email] = email
                it[createdAt] = UtcNow()
            } get DbUser.id

            if (passwordHash != null && salt != null) {
                Password.insert {
                    it[Password.userId] = userId
                    it[Password.passwordHash] = passwordHash
                    it[Password.salt] = salt
                    it[Password.lastChanged] = UtcNow()
                }
            }
        }
    }

    /** Retrieve a user by username. */
    fun getUserByName(username: String): User? {
        return transaction {
            DbUser.select { DbUser.name eq username }
                .singleOrNull()
                ?.let {
                    User(
                        id = it[DbUser.id],
                        name = it[DbUser.name],
                        email = it[DbUser.email],
                        groupId = it[DbUser.groupId],
                        roleId = it[DbUser.roleId],
                        statusId = it[DbUser.statusId],
                        createdAt = it[DbUser.createdAt],
                        passwordHash = Password.select { Password.userId eq it[DbUser.id] }
                            .single()[Password.passwordHash],
                        salt = Password.select { Password.userId eq it[DbUser.id] }
                            .single()[Password.salt]
                    )
                }
        }
    }
}