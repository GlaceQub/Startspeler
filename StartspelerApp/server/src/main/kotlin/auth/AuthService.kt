package auth

import at.favre.lib.crypto.bcrypt.BCrypt
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 128)
    override val primaryKey = PrimaryKey(id)
}

class AuthService {
    /** Hash a plain password. Default cost is 12. */
    fun hashPassword(plain: String, cost: Int = 12): String =
        BCrypt.withDefaults().hashToString(cost, plain.toCharArray())

    /** Verify a plain password against a stored bcrypt hash. */
    fun verifyPassword(plain: String, hash: String): Boolean =
        BCrypt.verifyer().verify(plain.toCharArray(), hash).verified

    /** Authenticate a user by username/password using Exposed. */
    fun authenticate(username: String, password: String): Boolean {
        return transaction {
            val row = Users.select { Users.username eq username }.singleOrNull()
            val storedHash = row?.get(Users.passwordHash) ?: return@transaction false
            verifyPassword(password, storedHash)
        }
    }
}

