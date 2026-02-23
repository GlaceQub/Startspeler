package startspeler.server.repository

import com.startspeler.dto.OrderOverzichtItem
import com.startspeler.dto.OrderItemRequest
import com.startspeler.dto.OverzichtOrderitem
import db.tables.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object OrderRepository {

    fun getAll(from: String? = null, to: String? = null): List<OrderOverzichtItem> = transaction {
        // Parseer "yyyy-MM-ddTHH:mm" of "yyyy-MM-ddTHH:mm:ss" naar LocalDateTime
        fun parseDateTime(s: String): LocalDateTime? = try {
            // Normaliseer: zorg dat seconden aanwezig zijn
            val normalized = when {
                s.length == 16 -> "$s:00"   // "2026-02-23T00:00" -> "2026-02-23T00:00:00"
                s.length == 19 -> s          // al volledig
                else -> s
            }
            LocalDateTime.parse(normalized)
        } catch (_: Exception) {
            println("[OrderRepository] parseDateTime FAILED for: '$s'")
            null
        }

        val fromDt = from?.also { println("[OrderRepository] from param: '$it'") }?.let { parseDateTime(it) }
            .also { println("[OrderRepository] fromDt parsed: $it") }
        val toDt = to?.also { println("[OrderRepository] to param: '$to'") }?.let { parseDateTime(it) }
            .also { println("[OrderRepository] toDt parsed: $it") }

        Order.selectAll().mapNotNull { orderRow ->
            val createdAt = orderRow[Order.createdAt]
            if (fromDt != null && createdAt < fromDt) return@mapNotNull null
            if (toDt != null && createdAt > toDt) return@mapNotNull null

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
                createdAt = orderRow[Order.createdAt].toString()
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
