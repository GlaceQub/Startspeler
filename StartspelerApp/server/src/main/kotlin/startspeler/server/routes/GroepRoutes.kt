package startspeler.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.GroepRepository

fun Routing.groepRoutes() {
    route("/groepen") {
        get {
            call.respond(GroepRepository.getAll())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val groep = id?.let { GroepRepository.getGroupById(it) }
            if (groep != null) {
                call.respond(groep)
            } else {
                call.respondText("Groep not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}