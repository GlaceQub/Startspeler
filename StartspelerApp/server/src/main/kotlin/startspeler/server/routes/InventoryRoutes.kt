package startspeler.server.routes

import com.startspeler.dto.InventoryDto
import com.startspeler.dto.InventoryUpdateRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.InventoryRepository

fun Routing.inventoryRoutes() {
    route("/inventory") {
        get {
            val items = InventoryRepository.getAll().map { inv ->
                InventoryDto(
                    id = inv.id,
                    productId = inv.productId,
                    quantity = inv.quantity,
                    minimumQuantity = inv.minimumQuantity,
                    lastUpdated = inv.lastUpdated?.toString()
                )
            }
            call.respond(items)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

            val item = InventoryRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Inventory not found")

            call.respond(InventoryDto(
                id = item.id,
                productId = item.productId,
                quantity = item.quantity,
                minimumQuantity = item.minimumQuantity,
                lastUpdated = item.lastUpdated?.toString()
            ))
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val req = call.receive<InventoryUpdateRequest>()

            val rows = InventoryRepository.update(id, req.quantity, req.minimumQuantity)
            if (rows == 0) {
                return@put call.respond(HttpStatusCode.NotFound, "Inventory not found")
            }

            val updated = InventoryRepository.getById(id)!!
            call.respond(InventoryDto(
                id = updated.id,
                productId = updated.productId,
                quantity = updated.quantity,
                minimumQuantity = updated.minimumQuantity,
                lastUpdated = updated.lastUpdated?.toString()
            ))
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")

            val rows = InventoryRepository.delete(id)
            if (rows == 0) {
                return@delete call.respond(HttpStatusCode.NotFound, "Inventory not found")
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}