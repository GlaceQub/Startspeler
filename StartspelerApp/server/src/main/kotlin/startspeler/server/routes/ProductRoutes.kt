package startspeler.server.routes

import com.startspeler.dto.ProductCreateUpdateDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import startspeler.server.repository.ProductRepository

fun Routing.productRoutes() {
    route("/products") {
        get {
            call.respond(ProductRepository.getAll())
        }

        post {
            val req = call.receive<ProductCreateUpdateDto>()
            val name = req.name.trim()

            if (name.isBlank()) {
                call.respondText("Name is required", status = HttpStatusCode.BadRequest)
                return@post
            }

            if (ProductRepository.existsByNameAndCategory(name, req.categoryId)) {
                call.respondText("Product bestaat al", status = HttpStatusCode.Conflict)
                return@post
            }

            val created = ProductRepository.create(
                name = name,
                categoryId = req.categoryId,
                price = req.price,
                popularity = req.popularity
            )
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Product ID is required", status = HttpStatusCode.BadRequest)
                return@put
            }

            val req = call.receive<ProductCreateUpdateDto>()
            val name = req.name.trim()

            if (name.isBlank()) {
                call.respondText("Name is required", status = HttpStatusCode.BadRequest)
                return@put
            }

            if (ProductRepository.existsByNameAndCategory(name, req.categoryId, excludeId = id)) {
                call.respondText("Product bestaat al", status = HttpStatusCode.Conflict)
                return@put
            }

            val updated = ProductRepository.update(
                id = id,
                name = name,
                categoryId = req.categoryId,
                price = req.price,
                popularity = req.popularity
            )

            if (updated == null) call.respondText("Product not found", status = HttpStatusCode.NotFound)
            else call.respond(updated)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Product ID is required", status = HttpStatusCode.BadRequest)
                return@delete
            }

            val ok = ProductRepository.delete(id)
            if (!ok) call.respondText("Product not found", status = HttpStatusCode.NotFound)
            else call.respondText("Deleted", status = HttpStatusCode.OK)
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