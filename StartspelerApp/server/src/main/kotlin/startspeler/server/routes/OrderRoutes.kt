package startspeler.server.routes

import com.startspeler.dto.BulkCheckoutRequest
import com.startspeler.dto.PlaceOrderRequest
import com.startspeler.dto.UpdateOrderRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.OrderRepository

fun Routing.orderRoutes() {
    route("/order") {

        get("/all") {
            val from = call.request.queryParameters["from"]
            val to = call.request.queryParameters["to"]
            val orders = OrderRepository.getAll(from, to)
            call.respond(orders)
        }

        get("/open-by-client") {
            val clientName = call.request.queryParameters["clientName"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "clientName is verplicht"))
            val summary = OrderRepository.getOpenOrdersForClient(clientName)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Klant niet gevonden"))
            call.respond(summary)
        }

        post("/checkout-client") {
            val req = call.receive<BulkCheckoutRequest>()
            val result = OrderRepository.checkoutOpenOrdersForClient(req.clientName)
            if (result.success) {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.BadRequest, result)
            }
        }

        // Single order ophalen voor edit page
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldige order id"))
            val order = OrderRepository.getById(id)
            if (order == null) call.respond(HttpStatusCode.NotFound, mapOf("error" to "Order niet gevonden"))
            else call.respond(order)
        }

        post("/add") {
            val req = call.receive<PlaceOrderRequest>()

            val userId = OrderRepository.getUserIdByName(req.klant)
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Klant niet gevonden"))

            val tafelNum = req.tafel.filter { it.isDigit() }.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldig tafelnummer"))

            val tableId = OrderRepository.getTableIdByNumber(tafelNum)
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Tafel niet gevonden"))

            val totalPrice = req.items.sumOf { it.price.toDouble() * it.quantity.toDouble() }.toFloat()

            val groupDiscount = OrderRepository.getGroupDiscount(userId)
            val priceAfterDiscount = if (groupDiscount > 0.0f) {
                totalPrice * (1.0f - groupDiscount / 100.0f)
            } else totalPrice

            val principal = call.principal<JWTPrincipal>()
            val isPlacedByStaff = principal != null

            val orderId = OrderRepository.add(userId, tableId, totalPrice, priceAfterDiscount, isPlacedByStaff, req.items)
            when (orderId) {
                is OrderRepository.AddResult.Success ->
                    call.respond(HttpStatusCode.Created, mapOf("orderId" to orderId.orderId))
                is OrderRepository.AddResult.InsufficientStock ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "insufficient_stock", "products" to orderId.productNames))
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldige order id"))
            val req = call.receive<UpdateOrderRequest>()

            val userId = OrderRepository.getUserIdByName(req.klant)
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Klant niet gevonden"))

            val tafelNum = req.tafel.filter { it.isDigit() }.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldig tafelnummer"))

            val tableId = OrderRepository.getTableIdByNumber(tafelNum)
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Tafel niet gevonden"))

            val totalPrice = req.items.sumOf { it.price.toDouble() * it.quantity.toDouble() }.toFloat()
            val groupDiscount = OrderRepository.getGroupDiscount(userId)
            val priceAfterDiscount = if (groupDiscount > 0.0f) {
                totalPrice * (1.0f - groupDiscount / 100.0f)
            } else totalPrice

            when (val res = OrderRepository.updateOrder(id, userId, tableId, totalPrice, priceAfterDiscount, req.items)) {
                is OrderRepository.AddResult.Success -> {
                    val updatedOrder = OrderRepository.getById(res.orderId)
                    if (updatedOrder == null) {
                        call.respond(HttpStatusCode.OK, mapOf("orderId" to res.orderId))
                    } else {
                        call.respond(HttpStatusCode.OK, updatedOrder)
                    }
                }
                is OrderRepository.AddResult.InsufficientStock -> {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "insufficient_stock", "products" to res.productNames))
                }
            }
        }

        post("/{id}/status/next") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldige order id"))
            val result = OrderRepository.transitionOrderStatus(id, "next")
            if (result.success) call.respond(HttpStatusCode.OK, result)
            else call.respond(HttpStatusCode.BadRequest, result)
        }

        post("/{id}/status/previous") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldige order id"))
            val result = OrderRepository.transitionOrderStatus(id, "previous")
            if (result.success) call.respond(HttpStatusCode.OK, result)
            else call.respond(HttpStatusCode.BadRequest, result)
        }

        post("/{id}/checkout") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldige order id"))
            val updated = OrderRepository.checkoutOrder(id)
            if (updated) {
                call.respond(HttpStatusCode.OK, mapOf("success" to true))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Order niet gevonden of status niet geldig voor afrekenen"))
            }
        }

        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Niet ingelogd"))
            val role = principal.payload.getClaim("role").asString()?.lowercase()
            if (role != "medewerker" && role != "beheerder") {
                return@delete call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Onvoldoende rechten"))
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldige order id"))
            val result = OrderRepository.deleteOrder(id)
            if (result.success) {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.BadRequest, result)
            }
        }

        post("/{id}/inbehandeling") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Ongeldige order id"))
            val result = OrderRepository.transitionOrderStatus(id, "next")
            if (result.success) {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.BadRequest, result)
            }
        }
    }
}
