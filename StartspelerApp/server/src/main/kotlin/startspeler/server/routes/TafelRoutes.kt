package startspeler.server.routes

import com.startspeler.server.repository.TafelRepository
import com.startspeler.tables.dto.TafelUpdate
import io.ktor.server.routing.put
import com.startspeler.tables.dto.Table
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import db.tables.TableModel
import com.startspeler.tables.dto.TafelCreate
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive

fun Routing.tafelRoutes() {
    route("/tafels") {
        get {
            // Return all tafel numbers as strings (e.g. "Tafel 1")
            val tafels = transaction {
                TableModel.selectAll().map { "Tafel ${it[TableModel.number]}" }
            }
            call.respond(tafels)
        }

        get("/all") {
            val result: List<Table> = transaction {
                TafelRepository.getAll().map { t ->
                    Table(
                        id = t.id,
                        number = t.number,
                        statusId = t.statusId,
                        statusName = t.statusName
                    )
                }
            }
            call.respond(result)
        }

        post {
            val req = call.receive<TafelCreate>()

            val created: Table = transaction {
                val t = TafelRepository.create(
                    number = req.number,
                    statusId = req.statusId
                )
                Table(
                    id = t.id,
                    number = t.number,
                    statusId = t.statusId,
                    statusName = t.statusName
                )
            }

            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid id")
                return@put
            }

            val req = call.receive<TafelUpdate>()

            val updated: Table? = transaction {
                val t = TafelRepository.update(id, req.number, req.statusId) ?: return@transaction null
                Table(
                    id = t.id,
                    number = t.number,
                    statusId = t.statusId,
                    statusName = t.statusName
                )
            }

            if (updated == null) call.respond(HttpStatusCode.NotFound, "Table not found")
            else call.respond(updated)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid id")
                return@delete
            }

            val ok = transaction { TafelRepository.delete(id) }
            if (ok) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound, "Table not found")
        }
    }
}

