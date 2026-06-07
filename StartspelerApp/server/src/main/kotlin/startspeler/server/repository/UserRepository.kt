package startspeler.server.repository

import com.startspeler.models.User
import db.tables.Password
import db.tables.User as DbUser
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utils.UtcNow
import auth.PasswordManager
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq

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
     * Default groupId, roleId and statusId are provided (override if needed).
     */
    fun createUser(
        username: String,
        email: String? = null,
        passwordHash: String? = null,
        salt: String? = null,
        groupId: Int = 1,
        roleId: Int = 2,
        statusId: Int = 1
    ): Int {
        return transaction {
            // Insert user and capture returned id (may be Int or EntityID<Int> depending on table definition)
            val insertedIdAny = DbUser.insert {
                it[DbUser.name] = username
                it[DbUser.groupId] = groupId
                it[DbUser.roleId] = roleId
                it[DbUser.statusId] = statusId
                it[DbUser.email] = email
                it[DbUser.createdAt] = UtcNow()
            } get DbUser.id

            // Normalize to plain Int
            val userId: Int = when (insertedIdAny) {
                is EntityID<*> -> (insertedIdAny.value as? Int)
                    ?: throw IllegalStateException("Inserted id EntityID.value is not Int")
                is Int -> insertedIdAny
                else -> throw IllegalStateException("Unexpected inserted id type: ${insertedIdAny?.javaClass}")
            }

            if (passwordHash != null && salt != null) {
                Password.insert {
                    // Password.userId column may expect Int (or EntityID) depending on schema.
                    // Using Int here; adjust if your Password.userId is defined as a reference type.
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
    fun getUserByName(username: String): User? {
        return transaction {
            DbUser.select { DbUser.name eq username }
                .singleOrNull()
                ?.let { row -> mapRowToUser(row) }
        }
    }

    /** Check if a username exists. */
    fun userExists(username: String): Boolean {
        return transaction {
            DbUser.select { DbUser.name eq username }.count() > 0
        }
    }

    /** Helper: map ResultRow to your domain User model; tolerant for missing password row. */
    private fun mapRowToUser(row: ResultRow): User {
        // Resolve id (EntityID<Int> or Int)
        val idRaw = row[DbUser.id]
        val idValue: Int = when (idRaw) {
            is EntityID<*> -> (idRaw.value as? Int) ?: throw IllegalStateException("User id not Int")
            is Int -> idRaw
            else -> throw IllegalStateException("Unexpected id type: ${idRaw.javaClass}")
        }

        // Try to find password row (may be absent)
        val pwRow = Password.select { Password.userId eq idValue }.singleOrNull()
        val pwHashNullable = pwRow?.get(Password.passwordHash)
        val saltNullable = pwRow?.get(Password.salt)

        // If your domain User model expects non-null passwordHash/salt, provide safe defaults.
        // We use empty string fallback to keep compatibility with existing code that assumes non-null.
        val pwHash = pwHashNullable ?: ""
        val salt = saltNullable ?: ""

        // createdAt may be LocalDateTime (or other); pass through as-is
        val createdAtValue = row[DbUser.createdAt]

        return User(
            id = idValue,
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

    fun getAllUsers(name: String? = null, email: String? = null): List<User> = transaction {
        val q = DbUser.selectAll()
        val filtered = if (!name.isNullOrBlank() && !email.isNullOrBlank()) {
            DbUser.select { (DbUser.name like "%$name%") and (DbUser.email like "%$email%") }
        } else if (!name.isNullOrBlank()) {
            DbUser.select { DbUser.name like "%$name%" }
        } else if (!email.isNullOrBlank()) {
            DbUser.select { DbUser.email like "%$email%" }
        } else {
            q
        }
        filtered.map { row -> mapRowToUser(row) }
    }

    fun getUserById(id: Int): User? = transaction {
        DbUser.select { DbUser.id eq id }.singleOrNull()?.let { mapRowToUser(it) }
    }

    fun usernameExists(username: String, excludeId: Int? = null): Boolean = transaction {
        if (excludeId == null) DbUser.select { DbUser.name eq username }.count() > 0
        else DbUser.select { (DbUser.name eq username) and (DbUser.id neq excludeId) }.count() > 0
    }

    fun emailExists(email: String, excludeId: Int? = null): Boolean = transaction {
        if (excludeId == null) DbUser.select { DbUser.email eq email }.count() > 0
        else DbUser.select { (DbUser.email eq email) and (DbUser.id neq excludeId) }.count() > 0
    }

    fun updateUser(id: Int, username: String, email: String?, groupId: Int?): User? = transaction {
        val updated = DbUser.update({ DbUser.id eq id }) {
            it[DbUser.name] = username
            it[DbUser.email] = email
            if (groupId != null) it[DbUser.groupId] = groupId
        }
        if (updated == 0) null else getUserById(id)
    }

    fun deleteUser(id: Int): Boolean = transaction {
        DbUser.deleteWhere { DbUser.id eq id } > 0
    }
}