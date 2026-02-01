package startspeler.server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import db.tables.Order
import db.tables.Orderitem
import db.tables.User
import db.tables.TableModel
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import db.tables.Role
import db.tables.Product


@Serializable
data class OrderItemRequest(val productId: Int, val quantity: Int, val price: Float)

@Serializable
data class PlaceOrderRequest(val klant: String, val tafel: String, val items: List<OrderItemRequest>)

fun Routing.orderRoutes() {
    route("/order") {
        post("/add") {
            val req = call.receive<PlaceOrderRequest>()
            // Find userId by klant name
            val userId = transaction {
                User.select { User.name eq req.klant }.singleOrNull()?.get(User.id)
            }
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Klant niet gevonden"))
                return@post
            }
            // Find tableId by tafel string (e.g. "Tafel 1" -> 1)
            val tafelNum = req.tafel.filter { it.isDigit() }.toIntOrNull()
            val tableId = if (tafelNum != null) transaction {
                TableModel.select { TableModel.number eq tafelNum }.singleOrNull()?.get(TableModel.id)
            } else null
            if (tableId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Tafel niet gevonden"))
                return@post
            }
            // Calculate total price
            val totalPrice = req.items.sumOf { it.price.toDouble() * it.quantity.toDouble() }.toFloat()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            // Determine if placed by staff
            val principal = call.principal<JWTPrincipal>()
            var isPlacedByStaff = false // Default: not placed by staff
            if (principal != null) {
                val username = principal.payload.getClaim("username").asString()
                val klantRoleName = "klant"
                val userRole = transaction {
                    User.innerJoin(Role).select { User.name eq username }.singleOrNull()?.get(Role.name)
                }
                if (userRole != null && !userRole.equals(klantRoleName, ignoreCase = true)) {
                    isPlacedByStaff = true
                }
            }

            // Insert order
            val orderId = transaction {
                Order.insert {
                    it[Order.userId] = userId
                    it[Order.tableId] = tableId
                    it[Order.statusId] = 5
                    it[Order.totalPrice] = totalPrice
                    it[Order.priceAfterDiscount] = null
                    it[Order.createdAt] = now
                    it[Order.isPlacedByStaff] = isPlacedByStaff
                    it[Order.remarks] = null
                } get Order.id
            }
            // Insert order items and update product popularity
            transaction {
                req.items.forEach { item ->
                    Orderitem.insert {
                        it[Orderitem.orderId] = orderId
                        it[Orderitem.productId] = item.productId
                        it[Orderitem.quantity] = item.quantity
                        it[Orderitem.price] = item.price
                    }
                    // Update product popularity
                    val currentPopularity = Product.select { Product.id eq item.productId }
                        .singleOrNull()?.get(Product.popularity) ?: 0
                    Product.update({ Product.id eq item.productId }) {
                        it[Product.popularity] = (currentPopularity ?: 0) + item.quantity
                    }
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("orderId" to orderId))
        }
    }
}
