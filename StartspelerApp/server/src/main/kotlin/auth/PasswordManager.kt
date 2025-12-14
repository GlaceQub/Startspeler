package auth

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.SecureRandom
import java.util.Base64

class PasswordManager {
    companion object {
        /** Generate a random salt. */
        fun generateSalt(length: Int = 16): String {
            val random = SecureRandom()
            val salt = ByteArray(length)
            random.nextBytes(salt)
            return Base64.getEncoder().encodeToString(salt)
        }

        /** Hash a plain password with salt using bcrypt. */
        fun hashPasswordWithSalt(plain: String, salt: String, cost: Int = 12): String {
            val salted = plain + salt
            return BCrypt.withDefaults().hashToString(cost, salted.toCharArray())
        }

        /** Verify a plain password against a stored bcrypt hash with salt. */
        fun verifyPasswordWithSalt(plain: String, salt: String, hash: String): Boolean {
            val salted = plain + salt
            return BCrypt.verifyer().verify(salted.toCharArray(), hash).verified
        }
    }
}
