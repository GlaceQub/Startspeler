package startspeler.server.repository

import com.startspeler.models.User
import db.tables.Password
import db.tables.User as DbUser
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.min
import utils.UtcNow
import auth.PasswordManager

/**
 * Repository for users. Contains helper exceptions as nested classes so existing imports like
 * UserRepository.NotFound / DuplicateName etc keep working.
 */
object UserRepository {

    // Exceptions (nested so routes can import them as UserRepository.NotFound etc)
    class NotFound(msg: String = "not found") : RuntimeException(msg)
    class DuplicateName(msg: String = "duplicate name") : RuntimeException(msg)
    class DuplicateEmail(msg: String = "duplicate email") : RuntimeException(msg)
    class SimilarName(val candidates: List<String>) : RuntimeException("similar names exist")

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
            val inserted = DbUser.insert {
                it[DbUser.name] = username
                it[DbUser.groupId] = groupId
                it[DbUser.roleId] = roleId
                it[DbUser.statusId] = statusId
                it[DbUser.email] = email
                it[DbUser.createdAt] = UtcNow()
            } get DbUser.id

            // Normalize to Int (handle EntityID or Int)
            val userId = when (inserted) {
                is EntityID<*> -> (inserted.value as? Int) ?: throw IllegalStateException("Inserted id not Int")
                is Int -> inserted
                else -> throw IllegalStateException("Unexpected inserted id type: ${inserted?.javaClass}")
            }

            if (passwordHash != null && salt != null) {
                Password.insert {
                    // if Password.userId column expects EntityID, adjust accordingly; using Int here
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
                ?.let { mapRowToUser(it) }
        }
    }

    /** Check if a username exists. */
    fun userExists(username: String): Boolean {
        return transaction {
            DbUser.select { DbUser.name eq username }.count() > 0
        }
    }

    /** Search users by name or email (simple). */
    fun searchUsers(query: String?): List<User> {
        return transaction {
            val rows = if (query.isNullOrBlank()) {
                DbUser.selectAll().toList()
            } else {
                val q = "%${query.trim()}%"
                DbUser.select { (DbUser.name like q) or (DbUser.email like q) }.toList()
            }
            rows.map { mapRowToUser(it) }
        }
    }

    /**
     * Update a user. Parameters are nullable — when null the existing value is kept.
     * Throws DuplicateName, DuplicateEmail, SimilarName, NotFound accordingly.
     * Returns updated User.
     */
    fun updateUser(
        userId: Int,
        name: String? = null,
        email: String? = null,
        groupId: Int? = null,
        roleId: Int? = null,
        statusId: Int? = null,
        confirmSimilar: Boolean = false
    ): User {
        return transaction {
            val existingRow = DbUser.select { DbUser.id eq userId }.singleOrNull()
                ?: throw NotFound("User $userId not found")

            val currentName = existingRow[DbUser.name]
            val currentEmail = existingRow[DbUser.email]
            val newName = name ?: currentName
            val newEmail = email ?: currentEmail

            // exact duplicate name (other user)
            val nameDup = DbUser.select { (DbUser.name eq newName) and (DbUser.id neq userId) }.count() > 0
            if (nameDup) throw DuplicateName("Name already registered")

            // exact duplicate email (other user)
            if (!newEmail.isNullOrBlank()) {
                val emailDup = DbUser.select { (DbUser.email eq newEmail) and (DbUser.id neq userId) }.count() > 0
                if (emailDup) throw DuplicateEmail("Email already registered")
            }

            // Similar name detection (Levenshtein <= 2)
            val similar = DbUser.select { DbUser.id neq userId }.map { it[DbUser.name] }
                .filter { levenshtein(it.lowercase(), newName.lowercase()) <= 2 }
            if (similar.isNotEmpty() && !confirmSimilar) throw SimilarName(similar)

            // Perform update: only set fields provided (not-null)
            DbUser.update({ DbUser.id eq userId }) { st ->
                name?.let { st[DbUser.name] = it }
                email?.let { st[DbUser.email] = it }
                groupId?.let { st[DbUser.groupId] = it }
                roleId?.let { st[DbUser.roleId] = it }
                statusId?.let { st[DbUser.statusId] = it }
            }

            val updatedRow = DbUser.select { DbUser.id eq userId }.single()
            mapRowToUser(updatedRow)
        }
    }

    fun deleteUser(userId: Int) {
        transaction {
            // delete password row first (if any)
            Password.deleteWhere { Password.userId eq userId }
            DbUser.deleteWhere { DbUser.id eq userId }
        }
    }

    fun assignGroup(userId: Int, groupId: Int) {
        transaction {
            val exists = DbUser.select { DbUser.id eq userId }.singleOrNull() ?: throw NotFound("User not found")
            DbUser.update({ DbUser.id eq userId }) {
                it[DbUser.groupId] = groupId
            }
        }
    }

    /** Helper: map ResultRow to domain User */
    private fun mapRowToUser(row: ResultRow): User {
        val idRaw = row[DbUser.id]
        val idValue: Int = when (idRaw) {
            is EntityID<*> -> (idRaw.value as? Int) ?: throw IllegalStateException("User id not Int")
            is Int -> idRaw
            else -> throw IllegalStateException("Unexpected id type: ${idRaw.javaClass}")
        }

        val pwRow = Password.select { Password.userId eq idValue }.singleOrNull()
        val pwHash = pwRow?.get(Password.passwordHash) ?: ""
        val salt = pwRow?.get(Password.salt) ?: ""
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

    // Simple Levenshtein distance for similarity detection
    private fun levenshtein(a: String, b: String): Int {
        val da = a.length
        val db = b.length
        if (da == 0) return db
        if (db == 0) return da
        val v0 = IntArray(db + 1) { it }
        val v1 = IntArray(db + 1)
        for (i in 0 until da) {
            v1[0] = i + 1
            for (j in 0 until db) {
                val cost = if (a[i] == b[j]) 0 else 1
                v1[j + 1] = min(min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost)
            }
            for (j in 0..db) v0[j] = v1[j]
        }
        return v1[db]
    }
}