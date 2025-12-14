package auth

import auth.PasswordManager
import auth.UserManager

class AuthService {
    /** Authenticate a user by username/password using UserManager and PasswordManager. */
    fun authenticate(username: String, password: String): Boolean {
        val user = UserManager.getUserByName(username) ?: return false
        return PasswordManager.verifyPasswordWithSalt(password, user.salt, user.passwordHash)
    }
}

