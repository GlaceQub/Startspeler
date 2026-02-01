package startspeler.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.ProductRepository

fun Routing.productRoutes() {
    route("/products") {
        get {
            call.respond(ProductRepository.getAll())
        }
        get("/top") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 3
            call.respond(ProductRepository.getTopPopularity().take(limit))
        }
        get("/top/category/{categoryId}") {
            val categoryId = call.parameters["categoryId"]?.toIntOrNull()
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 3
            if (categoryId == null) {
                call.respondText("Category ID is required", status = HttpStatusCode.BadRequest)
            } else {
                call.respond(ProductRepository.getTopPopularityCategory(categoryId, limit))
            }
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val product = id?.let { ProductRepository.getById(it) }
            if (product != null) {
                call.respond(product)
            } else {
                call.respondText("Product not found", status = HttpStatusCode.NotFound)
            }
        }
        get("/category/{categoryId}") {
            val categoryId = call.parameters["categoryId"]?.toIntOrNull()
            if (categoryId == null) {
                call.respondText("Category ID is required", status = HttpStatusCode.BadRequest)
            } else {
                call.respond(ProductRepository.getByCategoryId(categoryId))
            }
        }
        get("/category/{categoryId}/with-stock") {
            val categoryId = call.parameters["categoryId"]?.toIntOrNull()
            if (categoryId == null) {
                call.respondText("Category ID is required", status = HttpStatusCode.BadRequest)
            } else {
                call.respond(ProductRepository.getAllByCategoryWithStock(categoryId))
            }
        }
    }
}