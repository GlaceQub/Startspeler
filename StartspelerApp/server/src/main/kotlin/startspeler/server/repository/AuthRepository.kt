package startspeler.server.repository

import startspeler.server.repository.UserRepository
import auth.PasswordManager

object AuthRepository {
    fun authenticate(username: String, password: String): Boolean {
        val user = UserRepository.getUserByName(username) ?: return false
        return PasswordManager.verifyPasswordWithSalt(password, user.salt, user.passwordHash)
    }
}
