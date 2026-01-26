package startspeler.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.CategoryRepository

fun Routing.categoryRoutes() {
    route("/categories") {
        get {
            call.respond(CategoryRepository.getAll())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val category = id?.let { CategoryRepository.getById(it) }
            if (category != null) {
                call.respond(category)
            } else {
                call.respondText("Category not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
