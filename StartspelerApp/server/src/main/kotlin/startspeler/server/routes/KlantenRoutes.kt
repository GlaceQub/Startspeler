package startspeler.server.routes

import com.startspeler.klanten.dto.KlantAddRequestDto
import com.startspeler.klanten.dto.KlantUpdateRequestDto
import com.startspeler.klanten.dto.KlantenDto
import db.tables.Role
import db.tables.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import startspeler.server.repository.KlantEmailAlreadyExistsException
import startspeler.server.repository.KlantNameAlreadyExistsException
import startspeler.server.repository.KlantNotFoundException
import startspeler.server.repository.KlantenRepository
import startspeler.server.repository.UserRepository

fun Routing.klantenRoutes() {
    route("/klant") {
        post("/add") {
            val klant = call.receive<KlantAddRequestDto>()

            if (klant.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Naam is verplicht")
                return@post
            }

            if (KlantenRepository.nameExists(klant.name)) {
                call.respond(HttpStatusCode.Conflict, "Naam van klant is al geregistreerd")
                return@post
            }

            val email = klant.email
            if (!email.isNullOrBlank() && KlantenRepository.emailExists(email)) {
                call.respond(HttpStatusCode.Conflict, "E-mailadres is al geregistreerd")
                return@post
            }

            val id = UserRepository.createUser(
                username = klant.name,
                email = email,
                passwordHash = null,
                salt = null,
                groupId = 1,
                roleId = 3,
                statusId = 1
            )

            call.respond(HttpStatusCode.Created, mapOf("message" to "Klant added", "id" to id, "klant" to klant))
        }
    }

    route("/klanten") {
        get {
            val name = call.request.queryParameters["name"]
            val email = call.request.queryParameters["email"]

            val klanten = KlantenRepository.getAll(nameFilter = name, emailFilter = email)
                .map {
                    KlantenDto(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        groupId = it.groupId,
                        roleId = it.roleId,
                        statusId = it.statusId
                    )
                }

            call.respond(klanten)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid id")
                return@put
            }

            val req = call.receive<KlantUpdateRequestDto>()

            try {
                val updated = KlantenRepository.update(
                    id = id,
                    name = req.name,
                    email = req.email,
                    groupId = req.groupId
                )

                call.respond(
                    KlantenDto(
                        id = updated.id,
                        name = updated.name,
                        email = updated.email,
                        groupId = updated.groupId,
                        roleId = updated.roleId,
                        statusId = updated.statusId
                    )
                )
            } catch (e: KlantNotFoundException) {
                call.respond(HttpStatusCode.NotFound, e.message ?: "Geen klant gevonden")
            } catch (e: KlantNameAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "Naam bestaat al")
            } catch (e: KlantEmailAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "E-mail bestaat al")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Validatiefout")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid id")
                return@delete
            }

            val ok = KlantenRepository.delete(id)
            if (ok) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound, "Geen klant gevonden")
        }

        get("/names") {
            val klanten = transaction {
                (User innerJoin Role)
                    .select { (Role.name eq "klant") and (User.statusId eq 1) }
                    .map { it[User.name] }
            }
            call.respond(klanten)
        }
    }
}