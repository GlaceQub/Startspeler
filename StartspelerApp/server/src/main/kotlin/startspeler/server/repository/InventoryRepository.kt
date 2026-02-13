package startspeler.server.repository

import com.startspeler.models.Inventory
import db.tables.Inventory as InventoryTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction




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
}
