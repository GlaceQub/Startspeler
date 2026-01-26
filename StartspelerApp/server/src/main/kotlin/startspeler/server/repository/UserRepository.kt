package startspeler.server.repository

import com.startspeler.models.User
import db.tables.Password
import db.tables.User as DbUser
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.dao.id.EntityID
import utils.UtcNow
import auth.PasswordManager
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
                createUser(
                    username = defaultAdminUsername,
                    email = null,
                    passwordHash = passwordHash,
                    salt = salt,
                    groupId = 1,
                    roleId = 1,
                    statusId = 1
                )
            }
        }
    }

    /**
     * Create a new user with optional password and salt.
     * Returns newly inserted user id as Int.
     */
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
    ): Int {
        return transaction {
            val insertedIdAny = DbUser.insert {
                it[DbUser.name] = username
    ) {
        transaction {
            val userId = DbUser.insert {
                it[name] = username
                it[DbUser.groupId] = groupId
                it[DbUser.roleId] = roleId
                it[DbUser.statusId] = statusId
                it[DbUser.email] = email
                it[DbUser.createdAt] = UtcNow()
            } get DbUser.id

            // insertedIdAny kan Int of EntityID<Int> zijn; behandel beide zonder dynamic
            val userId = when (insertedIdAny) {
                is Int -> insertedIdAny
                is EntityID<*> -> (insertedIdAny.value as? Int)
                    ?: throw IllegalStateException("Inserted id EntityID.value is not Int")
                else -> throw IllegalStateException("Unexpected inserted id type: ${insertedIdAny?.javaClass}")
            }

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

            userId
        }
    }

    /** Retrieve a user by username. Returns null if not found. */
    fun getUserByName(username: String): UserModel? {
        return transaction {
            DbUser.select { DbUser.name eq username }
                .singleOrNull()
                ?.let { row ->
                    // password row kan ontbreken
                    val pwRow = Password.select { Password.userId eq row[DbUser.id] }.singleOrNull()
                    val pwHash = pwRow?.get(Password.passwordHash)
                    val salt = pwRow?.get(Password.salt)

                    // convert createdAt (LocalDateTime or similar) to String
                    val createdAtValue = row[DbUser.createdAt].toString()

                    UserModel(
                        id = row[DbUser.id],
                        name = row[DbUser.name],
                        email = row[DbUser.email],
                        groupId = row[DbUser.groupId],
                        roleId = row[DbUser.roleId],
                        statusId = row[DbUser.statusId],
                        createdAt = createdAtValue,
                        passwordHash = pwHash,
                        salt = salt
                    )
                }
        }
    }

    /** Check if a username exists. */
    fun userExists(username: String): Boolean {
        return transaction {
            DbUser.select { DbUser.name eq username }.count() > 0
        }
    }
}
