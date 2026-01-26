package startspeler.server.routes

import db.tables.Role
import db.tables.User
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import startspeler.server.repository.UserRepository
import org.jetbrains.exposed.sql.and

@Serializable
data class KlantAddRequest(val name: String, val email: String? = null)

fun Routing.klantenRoutes() {
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
    route("/klanten") {
        get {
            // Return all users with role 'klant' and statusId == 1 (active)
            val klanten = transaction {
                (User innerJoin Role).select { (Role.name eq "klant") and (User.statusId eq 1) }
                    .map { it[User.name] }
            }
            call.respond(klanten)
        }
    }
}