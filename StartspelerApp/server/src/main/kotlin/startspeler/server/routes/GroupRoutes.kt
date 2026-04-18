package startspeler.server.routes

import com.startspeler.dto.GroupMemberItem
import com.startspeler.dto.GroupOverviewItem
import com.startspeler.dto.GroupCreate
import com.startspeler.dto.GroupUpdate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import startspeler.server.repository.GroupAlreadyExistsException
import startspeler.server.repository.GroupRepository

fun Routing.groupRoutes() {
    route("/groups") {

        get {
            val result = transaction {
                GroupRepository.getAll().map { g ->
                    val (count, members) = GroupRepository.getMembersWithCount(g.id)
                    GroupOverviewItem(
                        id = g.id,
                        name = g.name,
                        discountPercentage = g.discount ?: 0f,
                        memberCount = count,
                        members = members.map { (uid, uname) -> GroupMemberItem(uid, uname) }
                    )
                }
            }
            call.respond(result)
        }

        post {
            val req = call.receive<GroupCreate>()
            try {
                val created = transaction {
                    val g = GroupRepository.create(req.name, req.discount)
                    GroupOverviewItem(
                        id = g.id,
                        name = g.name,
                        discountPercentage = g.discount ?: 0f,
                        memberCount = 0,
                        members = emptyList()
                    )
                }
                call.respond(HttpStatusCode.Created, created)
            } catch (e: GroupAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "Groep bestaat al")
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Ongeldig id")

            val req = call.receive<GroupUpdate>()
            try {
                val updated = transaction {
                    val g = GroupRepository.update(id, req.name, req.discount)
                        ?: return@transaction null
                    val (count, members) = GroupRepository.getMembersWithCount(g.id)
                    GroupOverviewItem(
                        id = g.id,
                        name = g.name,
                        discountPercentage = g.discount ?: 0f,
                        memberCount = count,
                        members = members.map { (uid, uname) -> GroupMemberItem(uid, uname) }
                    )
                }
                if (updated == null) call.respond(HttpStatusCode.NotFound, "Groep niet gevonden")
                else call.respond(updated)
            } catch (e: GroupAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "Groep bestaat al")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Ongeldig id")

            try {
                val ok = GroupRepository.delete(id)
                if (ok) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.BadRequest, "De standaard community kan niet verwijderd worden")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Verwijderen mislukt: ${e.message}")
            }
        }
    }
}
