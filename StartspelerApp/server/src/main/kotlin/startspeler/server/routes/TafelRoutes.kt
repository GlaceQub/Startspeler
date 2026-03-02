package startspeler.server.routes

import com.startspeler.server.repository.TafelRepository
import com.startspeler.tables.dto.Table
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import db.tables.TableModel

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
    }
}

