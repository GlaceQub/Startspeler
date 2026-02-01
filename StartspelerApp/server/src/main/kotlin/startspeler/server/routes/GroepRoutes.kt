package startspeler.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.GroepRepository

fun Routing.groepRoutes() {
    route("/groepen") {
        //Get /groepen
        get {
            call.respond(GroepRepository.getAll())
        }

        //Get /groepen/{id}
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respondText ("Invalid ID", status = HttpStatusCode.BadRequest)
                return@get
            }

            val groep = GroepRepository.getGroupById(id)

            if (groep != null) {
                call.respond(groep)
            } else {
                call.respondText("Groep not found", status = HttpStatusCode.NotFound)
            }

            val deleted = GroepRepository.deleteGroup(id)

            if (deleted) {
                call.respond(HttpStatusCode.OK, "Deleted")
            } else {
                call.respondText("Groep not found", status = HttpStatusCode.NotFound)
            }
        }

        // POST /groepen
        post("/groep/add") {
            try {
                // Receive JSON body as a Kotlin map
                val body = call.receive<Map<String, Any?>>()

                val name = body["name"] as? String
                val discount = (body["discount"] as? Number)?.toFloat()

                if (name.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Name is required")
                    return@post
                }

                // Add group via repository
                val newGroep = GroepRepository.addGroup(name, discount)

                // Respond with created group
                call.respond(HttpStatusCode.Created, newGroep)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid JSON body")
            }
        }


    }
}