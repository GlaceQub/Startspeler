package auth

import db.tables.Password
import db.tables.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utils.UtcNow

class UserManager {
    companion object {
        /** Create a default Admin user if none exists. */
        fun createDefaultAdminUser() {
            val defaultAdminUsername = "admin"
            transaction {
                val exists = User.select { User.name eq defaultAdminUsername }.count() > 0
                if (!exists) {
                    val defaultAdminPassword = "St4rtsp3l3r"
                    val salt = PasswordManager.generateSalt()
                    val passwordHash = PasswordManager.hashPasswordWithSalt(defaultAdminPassword, salt)
                    // Use default groupId, roleId, statusId = 1
                    createUser(defaultAdminUsername, passwordHash, salt, groupId = 1, roleId = 1, statusId = 1)
                }
            }
        }

        /** Create a new user with hashed password and salt. */
        fun createUser(
            username: String,
            passwordHash: String,
            salt: String,
            groupId: Int,
            roleId: Int,
            statusId: Int
        ) {
            transaction {
                val userId = User.insert {
                    it[name] = username
                    it[User.groupId] = groupId
                    it[User.roleId] = roleId
                    it[User.statusId] = statusId
                    it[email] = null // or set a default email if needed
                    it[createdAt] = UtcNow()
                } get User.id

                Password.insert {
                    it[Password.userId] = userId
                    it[Password.passwordHash] = passwordHash
                    it[Password.salt] = salt
                    it[Password.lastChanged] = UtcNow()
                }
            }
        }

        /** Retrieve a user by username. */
        fun getUserByName(username: String): entities.UserEntity? {
            return transaction {
                User.select { User.name eq username }
                    .singleOrNull()
                    ?.let {
                        entities.UserEntity(
                            id = it[User.id],
                            name = it[User.name],
                            email = it[User.email],
                            groupId = it[User.groupId],
                            roleId = it[User.roleId],
                            statusId = it[User.statusId],
                            createdAt = it[User.createdAt],
                            passwordHash = Password.select { Password.userId eq it[User.id] }
                                .single()[Password.passwordHash],
                            salt = Password.select { Password.userId eq it[User.id] }
                                .single()[Password.salt]
                        )
                    }
            }
        }
    }
}