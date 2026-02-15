package startspeler.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import startspeler.server.repository.GroepRepository

@Serializable
private data class CreateGroepRequest(val name: String, val discount: Float? = null)

fun Routing.groepRoutes() {
    route("/groepen") {
        // Get /groepen
        get {
            val all = GroepRepository.getAll()
            println("[DEBUG] /groepen returned ${'$'}{all.size} groups")
            call.respond(all)
        }

        // Get /groepen/{id}
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                return@get
            }

            val groep = GroepRepository.getGroupById(id)
            println("[DEBUG] /groepen/{id} fetched for id=${'$'}id -> ${'$'}groep")

            if (groep != null) {
                call.respond(groep)
            } else {
                call.respondText("Groep not found", status = HttpStatusCode.NotFound)
            }
        }

        // DELETE /groepen/{id}
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                return@delete
            }

            val deleted = GroepRepository.deleteGroup(id)

            if (deleted) {
                call.respond(HttpStatusCode.OK, "Deleted")
            } else {
                call.respondText("Groep not found", status = HttpStatusCode.NotFound)
            }
        }

        // POST /groepen/add
        post("/add") {
            try {
                val bodyText = call.receiveText()
                println("[DEBUG] POST /groepen/add body: ${'$'}bodyText")

                val req = try {
                    Json.decodeFromString(CreateGroepRequest.serializer(), bodyText)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON: ${'$'}{e.message}")
                    return@post
                }

                if (req.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Name is required")
                    return@post
                }

                val newGroep = GroepRepository.addGroup(req.name.trim(), req.discount)
                println("[DEBUG] Created group: ${'$'}newGroep")
                call.respond(HttpStatusCode.Created, newGroep)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${'$'}{e.message}")
            }
        }

    }

    // Backwards-compatible endpoint: POST /groep/add (root) -> forwards to /groepen/add logic
    route("/groep") {
        post("/add") {
            try {
                val bodyText = call.receiveText()
                println("[DEBUG] POST /groep/add body: ${'$'}bodyText")

                val req = try {
                    Json.decodeFromString(CreateGroepRequest.serializer(), bodyText)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON: ${'$'}{e.message}")
                    return@post
                }

                if (req.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Name is required")
                    return@post
                }

                val newGroep = GroepRepository.addGroup(req.name.trim(), req.discount)
                println("[DEBUG] Created group (legacy endpoint): ${'$'}newGroep")
                call.respond(HttpStatusCode.Created, newGroep)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${'$'}{e.message}")
            }
        }
    }
}