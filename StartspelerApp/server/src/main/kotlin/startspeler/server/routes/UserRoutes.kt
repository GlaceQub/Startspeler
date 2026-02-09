package startspeler.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import startspeler.server.repository.UserRepository
import startspeler.server.repository.UserRepository.DuplicateEmail
import startspeler.server.repository.UserRepository.DuplicateName
import startspeler.server.repository.UserRepository.NotFound
import startspeler.server.repository.UserRepository.SimilarName
import auth.PasswordManager

@Serializable
data class CreateUserRequest(val username: String, val email: String? = null, val password: String? = null)

@Serializable
data class CreateUserResponse(val id: Int)

@Serializable
data class UserUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    val groupId: Int? = null,
    val roleId: Int? = null,
    val statusId: Int? = null,
    val confirmSimilar: Boolean? = null
)

/**
 * Registers both public user creation routes and admin user management routes.
 *
 * Pass a function that verifies admin rights for admin routes:
 *   registerCombinedUserRoutes(::adminCheck)
 */
fun Route.registerCombinedUserRoutes(requireAdmin: suspend (ApplicationCall) -> Boolean) {
    // Public API endpoints
    route("/api") {
        post("/users") {
            val req = try { call.receive<CreateUserRequest>() } catch (t: Throwable) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid body"))
                return@post
            }

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
                } else {
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

    // Admin management endpoints
    route("/admin/users") {
        get {
            if (!requireAdmin(call)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            val q = call.request.queryParameters["q"]
            val users = UserRepository.searchUsers(q)
            call.respond(HttpStatusCode.OK, users)
        }

        put("/{id}") {
            if (!requireAdmin(call)) {
                call.respond(HttpStatusCode.Forbidden); return@put
            }
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val req = try { call.receive<UserUpdateRequest>() } catch (t: Throwable) {
                call.respond(HttpStatusCode.BadRequest); return@put
            }

            try {
                val updated = UserRepository.updateUser(
                    userId = id,
                    name = req.name,
                    email = req.email,
                    groupId = req.groupId,
                    roleId = req.roleId,
                    statusId = req.statusId,
                    confirmSimilar = req.confirmSimilar ?: false
                )
                call.respond(HttpStatusCode.OK, updated)
            } catch (e: SimilarName) {
                call.respond(HttpStatusCode.Conflict, mapOf("reason" to "similar_name", "candidates" to e.candidates))
            } catch (e: DuplicateName) {
                call.respond(HttpStatusCode.Conflict, mapOf("reason" to "duplicate_name"))
            } catch (e: DuplicateEmail) {
                call.respond(HttpStatusCode.Conflict, mapOf("reason" to "duplicate_email"))
            } catch (e: NotFound) {
                call.respond(HttpStatusCode.NotFound, mapOf("reason" to "not_found"))
            }
        }

        delete("/{id}") {
            if (!requireAdmin(call)) {
                call.respond(HttpStatusCode.Forbidden); return@delete
            }
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            try {
                UserRepository.deleteUser(id)
                call.respond(HttpStatusCode.OK)
            } catch (e: NotFound) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/{id}/group") {
            if (!requireAdmin(call)) {
                call.respond(HttpStatusCode.Forbidden); return@post
            }
            val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val body = try { call.receive<Map<String, Int>>() } catch (t: Throwable) {
                call.respond(HttpStatusCode.BadRequest); return@post
            }
            val groupId = body["groupId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            try {
                UserRepository.assignGroup(id, groupId)
                call.respond(HttpStatusCode.OK)
            } catch (e: NotFound) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}