package startspeler.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.InventoryRepository

fun Routing.inventoryRoutes() {
    route("/inventory") {
        get{
            val items = InventoryRepository.getAll()
            println("GET /inventory -> responding ${items.size} items")
            call.respond(items)
        }
    }
}