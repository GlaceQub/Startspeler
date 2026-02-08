package startspeler.server.routes

import startspeler.server.repository.UserRepository
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

            if (UserRepository.userExists(req.username)) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "username already exists"))
                return@post
            }

            val newId = try {
                if (!req.password.isNullOrBlank()) {
                    // Hash & salt the password then create user and get id
                    val salt = auth.PasswordManager.generateSalt()
                    val hash = auth.PasswordManager.hashPasswordWithSalt(req.password, salt)

                    UserRepository.createUser(
                        username = req.username,
                        email = req.email,
                        passwordHash = hash,
                        salt = salt,
                        groupId = 1,
                        roleId = 3,
                        statusId = 1
                    )
                } else {
                    // create without password
                    UserRepository.createUser(
                        username = req.username,
                        email = req.email,
                        passwordHash = null,
                        salt = null,
                        groupId = 1,
                        roleId = 2,
                        statusId = 1
                    )
                }
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to create user", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "could not create user"))
                return@post
            }

            call.respond(HttpStatusCode.Created, CreateUserResponse(id = newId))
        }
    }
}