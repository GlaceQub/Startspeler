package startspeler.server.routes

import startspeler.server.repository.UserRepository
import auth.PasswordManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(val username: String, val email: String? = null, val password: String? = null)

@Serializable
data class CreateUserResponse(val id: Int)

fun Route.registerUserRoutes() {
    route("/api") {
        post("/users") {
            val req = call.receive<CreateUserRequest>()

            if (req.username.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "username is required"))
                return@post
            }

            // voorkom dubbele usernames
            if (UserRepository.userExists(req.username)) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "username already exists"))
                return@post
            }

            try {
                if (!req.password.isNullOrBlank()) {
                    // hash & salt het wachtwoord en sla op via repository
                    val salt = PasswordManager.generateSalt()
                    val hash = PasswordManager.hashPasswordWithSalt(req.password, salt)

                    UserRepository.createUser(
                        username = req.username,
                        email = req.email,
                        passwordHash = hash,
                        salt = salt,
                        groupId = 1,
                        roleId = 2,
                        statusId = 1
                    )

                    // Omdat we een password hebben toegevoegd, kan getUserByName veilig de Password-rij verwachten
                    val created = UserRepository.getUserByName(req.username)
                    if (created != null) {
                        call.respond(HttpStatusCode.Created, CreateUserResponse(id = created.id))
                    } else {
                        // fallback, verwacht niet vaak voor te komen
                        call.respond(HttpStatusCode.Created, mapOf("message" to "user created"))
                    }
                } else {
                    // Geen wachtwoord: maak gebruiker zonder password-rij
                    UserRepository.createUser(
                        username = req.username,
                        email = req.email,
                        passwordHash = null,
                        salt = null,
                        groupId = 1,
                        roleId = 2,
                        statusId = 1
                    )

                    // createUser geeft geen id terug en getUserByName zou falen omdat er geen password-rij is,
                    // daarom sturen we een eenvoudige bevestiging terug.
                    call.respond(HttpStatusCode.Created, mapOf("message" to "user created"))
                }
            } catch (e: Exception) {
                // log en retourneer 500
                call.application.environment.log.error("Failed to create user", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "could not create user"))
            }
        }
    }
}