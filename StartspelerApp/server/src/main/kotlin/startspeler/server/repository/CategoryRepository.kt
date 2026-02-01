package startspeler.server.repository

import com.startspeler.models.Category
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import db.tables.Category as CategoryTable

object CategoryRepository {
    fun getAll(): List<Category> = transaction {
        CategoryTable.selectAll().map {
            Category(
                id = it[CategoryTable.id],
                name = it[CategoryTable.name]
            )
        }
    }

    fun getById(id: Int): Category? = transaction {
        CategoryTable.select { CategoryTable.id eq id }
            .map {
                Category(
                    id = it[CategoryTable.id],
                    name = it[CategoryTable.name]
                )
            }
            .singleOrNull()
    }
}


