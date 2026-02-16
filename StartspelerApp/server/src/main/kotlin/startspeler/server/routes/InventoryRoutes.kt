package startspeler.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import db.tables.Inventory as InventoryTable

@Serializable
data class InventoryDto(
    val id: Int,
    val productId: Int,
    val quantity: Int,
    val minimumQuantity: Int? = null,
    val lastUpdated: String? = null
)

@Serializable
data class InventoryCreateRequest(
    val productId: Int,
    val quantity: Int,
    val minimumQuantity: Int? = null
)

@Serializable
data class InventoryUpdateRequest(
    val quantity: Int,
    val minimumQuantity: Int? = null
)

fun Routing.inventoryRoutes() {
    route("/inventory") {
        get {
            val items = transaction {
                InventoryTable
                    .selectAll()
                    .orderBy(InventoryTable.id to SortOrder.ASC)
                    .map {
                        InventoryDto(
                            id = it[InventoryTable.id],
                            productId = it[InventoryTable.productId],
                            quantity = it[InventoryTable.quantity],
                            minimumQuantity = it[InventoryTable.minimumQuantity],
                            lastUpdated = it[InventoryTable.lastUpdated]?.toString()
                        )
                    }
            }
            call.respond(items)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

            val item = transaction {
                InventoryTable
                    .select { InventoryTable.id eq id }
                    .singleOrNull()
                    ?.let {
                        InventoryDto(
                            id = it[InventoryTable.id],
                            productId = it[InventoryTable.productId],
                            quantity = it[InventoryTable.quantity],
                            minimumQuantity = it[InventoryTable.minimumQuantity],
                            lastUpdated = it[InventoryTable.lastUpdated]?.toString()
                        )
                    }
            } ?: return@get call.respond(HttpStatusCode.NotFound, "Inventory not found")

            call.respond(item)
        }

        post("add") {
            val req = call.receive<InventoryCreateRequest>()

            val newId = transaction {
                InventoryTable.insert {
                    it[productId] = req.productId
                    it[quantity] = req.quantity
                    it[minimumQuantity] = req.minimumQuantity
                } get InventoryTable.id
            }

            val created = transaction {
                InventoryTable
                    .select { InventoryTable.id eq newId }
                    .single()
                    .let {
                        InventoryDto(
                            id = it[InventoryTable.id],
                            productId = it[InventoryTable.productId],
                            quantity = it[InventoryTable.quantity],
                            minimumQuantity = it[InventoryTable.minimumQuantity],
                            lastUpdated = it[InventoryTable.lastUpdated]?.toString()
                        )
                    }
            }

            call.respond(HttpStatusCode.Created, created)
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val req = call.receive<InventoryUpdateRequest>()

            val rows = transaction {
                InventoryTable.update({ InventoryTable.id eq id }) {
                    it[quantity] = req.quantity
                    it[minimumQuantity] = req.minimumQuantity
                }
            }
            if (rows == 0) {
                return@put call.respond(HttpStatusCode.NotFound, "Inventory not found")
            }

            val updated = transaction {
                InventoryTable
                    .select { InventoryTable.id eq id }
                    .single()
                    .let {
                        InventoryDto(
                            id = it[InventoryTable.id],
                            productId = it[InventoryTable.productId],
                            quantity = it[InventoryTable.quantity],
                            minimumQuantity = it[InventoryTable.minimumQuantity],
                            lastUpdated = it[InventoryTable.lastUpdated]?.toString()
                        )
                    }
            }

            call.respond(updated)
        }
    }
}