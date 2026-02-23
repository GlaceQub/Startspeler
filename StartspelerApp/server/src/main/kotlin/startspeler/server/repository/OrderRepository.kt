package startspeler.server.repository

import com.startspeler.dto.OrderOverzichtItem
import com.startspeler.dto.OrderItemRequest
import com.startspeler.dto.OverzichtOrderitem
import db.tables.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object OrderRepository {

    fun getAll(): List<OrderOverzichtItem> = transaction {
        Order.selectAll().map { orderRow ->
            val orderId = orderRow[Order.id]
            val clientName = User.select { User.id eq orderRow[Order.userId] }
                .singleOrNull()?.get(User.name) ?: "Onbekend"
            val tableNumber = TableModel.select { TableModel.id eq orderRow[Order.tableId] }
                .singleOrNull()?.get(TableModel.number) ?: 0
            val statusName = Status.select { Status.id eq orderRow[Order.statusId] }
                .singleOrNull()?.get(Status.name) ?: "Onbekend"
            val items = Orderitem.select { Orderitem.orderId eq orderId }.map { itemRow ->
                val productName = Product.select { Product.id eq itemRow[Orderitem.productId] }
                    .singleOrNull()?.get(Product.name) ?: "Onbekend"
                OverzichtOrderitem(
                    id = itemRow[Orderitem.id],
                    product = productName,
                    quantity = itemRow[Orderitem.quantity],
                    price = itemRow[Orderitem.price]
                )
            }
            OrderOverzichtItem(
                id = orderId,
                tableNumber = tableNumber,
                status = statusName,
                totalPrice = orderRow[Order.totalPrice],
                clientName = clientName,
                placedByStaff = orderRow[Order.isPlacedByStaff],
                orderitems = items,
                createdAt = orderRow[Order.createdAt].toString() // ISO 8601 string
            )
        }
    }

    fun add(
        userId: Int,
        tableId: Int,
        totalPrice: Float,
        priceAfterDiscount: Float,
        isPlacedByStaff: Boolean,
        items: List<OrderItemRequest>
    ): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val orderId = transaction {
            Order.insert {
                it[Order.userId] = userId
                it[Order.tableId] = tableId
                it[Order.statusId] = 5
                it[Order.totalPrice] = totalPrice
                it[Order.priceAfterDiscount] = priceAfterDiscount
                it[Order.createdAt] = now
                it[Order.isPlacedByStaff] = isPlacedByStaff
                it[Order.remarks] = null
            } get Order.id
        }
        transaction {
            items.forEach { item ->
                Orderitem.insert {
                    it[Orderitem.orderId] = orderId
                    it[Orderitem.productId] = item.productId
                    it[Orderitem.quantity] = item.quantity
                    it[Orderitem.price] = item.price
                }
                val currentPopularity = Product.select { Product.id eq item.productId }
                    .singleOrNull()?.get(Product.popularity) ?: 0
                Product.update({ Product.id eq item.productId }) {
                    it[Product.popularity] = currentPopularity + item.quantity
                }
                // Verminder stock in Inventory
                val inventoryRow = Inventory.select { Inventory.productId eq item.productId }.singleOrNull()
                if (inventoryRow != null) {
                    val currentStock = inventoryRow[Inventory.quantity]
                    val newStock = (currentStock - item.quantity).coerceAtLeast(0)
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    Inventory.update({ Inventory.productId eq item.productId }) {
                        it[Inventory.quantity] = newStock
                        it[Inventory.lastUpdated] = now
                    }
                }
            }
        }
        return orderId
    }

    fun getUserIdByName(name: String): Int? = transaction {
        User.select { User.name eq name }.singleOrNull()?.get(User.id)
    }

    fun getTableIdByNumber(number: Int): Int? = transaction {
        TableModel.select { TableModel.number eq number }.singleOrNull()?.get(TableModel.id)
    }

    fun getGroupDiscount(userId: Int): Float = transaction {
        val groupId = User.select { User.id eq userId }.singleOrNull()?.get(User.groupId)
        if (groupId != null) {
            Group.select { Group.id eq groupId }.singleOrNull()?.get(Group.discount) ?: 0.0f
        } else 0.0f
    }

    // Voeg checkoutOrder toe aan object OrderRepository
    fun checkoutOrder(orderId: Int): Boolean = transaction {
        Order.select { Order.id eq orderId }.singleOrNull() ?: return@transaction false
        val betaaldStatusId = Status.select { Status.name eq "betaald" }.singleOrNull()?.get(Status.id) ?: return@transaction false
        val updated = Order.update({ Order.id eq orderId }) {
            it[Order.statusId] = betaaldStatusId
        }
        updated > 0
    }

    fun setInBehandeling(orderId: Int): Boolean = transaction {
        Order.select { Order.id eq orderId }.singleOrNull() ?: return@transaction false
        val behandelingStatusId = Status.select { Status.name eq "in behandeling" }.singleOrNull()?.get(Status.id) ?: return@transaction false
        val updated = Order.update({ Order.id eq orderId }) {
            it[Order.statusId] = behandelingStatusId
        }
        updated > 0
    }
}
