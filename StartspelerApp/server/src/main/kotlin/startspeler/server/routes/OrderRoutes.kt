package startspeler.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import startspeler.server.repository.OrderRepository
import com.startspeler.dto.OrderItemRequest

@Serializable
data class PlaceOrderRequest(val klant: String, val tafel: String, val items: List<OrderItemRequest>)

fun Routing.orderRoutes() {
    route("/order") {

        get("/all") {
            val orders = OrderRepository.getAll()
            call.respond(orders)
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

            call.respond(HttpStatusCode.Created, mapOf("orderId" to orderId))
        }
    }
}
