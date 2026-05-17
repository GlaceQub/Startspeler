package startspeler.server.repository

import com.startspeler.models.Inventory
import db.tables.Inventory as InventoryTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import utils.DbUtcNow

object InventoryRepository {
    fun getAll(): List<Inventory> = transaction {
        InventoryTable.selectAll().map {
            Inventory(
                id = it[InventoryTable.id],
                productId = it[InventoryTable.productId],
                quantity = it[InventoryTable.quantity],
                minimumQuantity = it[InventoryTable.minimumQuantity],
                lastUpdated = it[InventoryTable.lastUpdated]
            )
        }
    }

    fun getById(id: Int): Inventory? = transaction {
        InventoryTable.select { InventoryTable.id eq id }.map {
            Inventory(
                id = it[InventoryTable.id],
                productId = it[InventoryTable.productId],
                quantity = it[InventoryTable.quantity],
                minimumQuantity = it[InventoryTable.minimumQuantity],
                lastUpdated = it[InventoryTable.lastUpdated]
            )
        }.singleOrNull()
    }

    fun getByProductId(productId: Int): Inventory? = transaction {
        InventoryTable.select { InventoryTable.productId eq productId }.map {
            Inventory(
                id = it[InventoryTable.id],
                productId = it[InventoryTable.productId],
                quantity = it[InventoryTable.quantity],
                minimumQuantity = it[InventoryTable.minimumQuantity],
                lastUpdated = it[InventoryTable.lastUpdated]
            )
        }.singleOrNull()
    }

    fun update(id: Int, quantity: Int, minimumQuantity: Int?): Int = transaction {
        InventoryTable.update({ InventoryTable.id eq id }) {
            it[InventoryTable.quantity] = quantity
            it[InventoryTable.minimumQuantity] = minimumQuantity
            it[InventoryTable.lastUpdated] = DbUtcNow()
        }
    }

    fun delete(id: Int): Int = transaction {
        InventoryTable.deleteWhere { InventoryTable.id eq id }
    }
}