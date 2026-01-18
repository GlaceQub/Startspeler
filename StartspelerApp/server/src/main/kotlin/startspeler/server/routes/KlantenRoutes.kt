package startspeler.server.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import startspeler.server.repository.UserRepository

@Serializable
data class KlantAddRequest(val name: String, val email: String? = null)

class KlantenRoutes {
    fun Route.klantenRoutes() {
        route("/klant") {
            post("/add") {
                val klant = call.receive<KlantAddRequest>()
                UserRepository.createUser(
                    username = klant.name,
                    passwordHash = null,
                    salt = null,
                    groupId = 1, // Standaard
                    roleId = 3,  // Klant
                    statusId = 1 // Actief
                )
                call.respond(HttpStatusCode.Created, mapOf("message" to "Klant added", "klant" to klant))
            }
        }
    }
}