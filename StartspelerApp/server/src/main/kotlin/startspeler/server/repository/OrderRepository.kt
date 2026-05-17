package startspeler.server.repository

import com.startspeler.dto.BulkCheckoutResponse
import com.startspeler.dto.ClientOpenOrdersSummary
import com.startspeler.dto.DeleteOrderResponse
import com.startspeler.dto.OrderOverzichtItem
import com.startspeler.dto.OrderItemRequest
import com.startspeler.dto.OrderStatusTransitionResponse
import com.startspeler.dto.OverzichtOrderitem
import db.tables.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.DbUtcNow
import utils.toUtcIsoInstantString

object OrderRepository {
    private const val STATUS_IN_BEHANDELING = 3
    private const val STATUS_BETAALD = 4
    private const val STATUS_AANGEMAAKT = 5
    private const val STATUS_AFGELEVERD = 6

    private val statusFlow = listOf(STATUS_AANGEMAAKT, STATUS_IN_BEHANDELING, STATUS_AFGELEVERD, STATUS_BETAALD)

    private fun previousStatusId(statusId: Int): Int? {
        val index = statusFlow.indexOf(statusId)
        return if (index > 0) statusFlow[index - 1] else null
    }

    private fun nextStatusId(statusId: Int): Int? {
        val index = statusFlow.indexOf(statusId)
        return if (index >= 0 && index < statusFlow.lastIndex) statusFlow[index + 1] else null
    }

    private fun statusNameById(statusId: Int): String? =
        Status.select { Status.id eq statusId }.singleOrNull()?.get(Status.name)

    private fun buildOrderOverviewItem(orderRow: ResultRow): OrderOverzichtItem {
        val orderId = orderRow[Order.id]
        val userId = orderRow[Order.userId]
        val clientName = User.select { User.id eq userId }
            .singleOrNull()?.get(User.name) ?: "Onbekend"
        val tableNumber = TableModel.select { TableModel.id eq orderRow[Order.tableId] }
            .singleOrNull()?.get(TableModel.number) ?: 0
        val statusId = orderRow[Order.statusId]
        val statusName = statusNameById(statusId) ?: "Onbekend"
        val previousStatusId = previousStatusId(statusId)
        val nextStatusId = nextStatusId(statusId)
        val items = Orderitem.select { Orderitem.orderId eq orderId }.map { itemRow ->
            val productRow = Product.select { Product.id eq itemRow[Orderitem.productId] }.singleOrNull()
            val productName = productRow?.get(Product.name) ?: "Onbekend"
            val productId = productRow?.get(Product.id) ?: itemRow[Orderitem.productId]
            OverzichtOrderitem(
                id = itemRow[Orderitem.id],
                product = productName,
                productId = productId,
                quantity = itemRow[Orderitem.quantity],
                price = itemRow[Orderitem.price]
            )
        }

        val canEdit = statusId == STATUS_AANGEMAAKT
        val canDelete = statusId == STATUS_AANGEMAAKT
        val canCheckout = statusId == STATUS_AFGELEVERD

        val groupId = User.select { User.id eq userId }.singleOrNull()?.get(User.groupId)
        val discountPct = if (groupId != null)
            Group.select { Group.id eq groupId }.singleOrNull()?.get(Group.discount) ?: 0f
        else 0f
        val totalPrice = orderRow[Order.totalPrice]
        val priceAfterDiscount = if (discountPct > 0f) totalPrice * (1f - discountPct / 100f) else totalPrice

        return OrderOverzichtItem(
            id = orderId,
            tableNumber = tableNumber,
            status = statusName,
            statusId = statusId,
            canGoToPreviousStatus = previousStatusId != null && statusId != STATUS_BETAALD,
            canGoToNextStatus = nextStatusId != null && statusId != STATUS_BETAALD,
            previousStatusLabel = previousStatusId?.let { statusNameById(it) },
            nextStatusLabel = nextStatusId?.let { statusNameById(it) },
            canEdit = canEdit,
            canDelete = canDelete,
            canCheckout = canCheckout,
            totalPrice = totalPrice,
            priceAfterDiscount = if (discountPct > 0f) priceAfterDiscount else null,
            discountPercentage = if (discountPct > 0f) discountPct else null,
            clientName = clientName,
            placedByStaff = orderRow[Order.isPlacedByStaff],
            orderitems = items,
            createdAt = orderRow[Order.createdAt].toUtcIsoInstantString()
        )
    }

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

            buildOrderOverviewItem(orderRow)
        }
    }

    sealed class AddResult {
        data class Success(val orderId: Int) : AddResult()
        data class InsufficientStock(val productNames: List<String>) : AddResult()
    }

    fun add(
        userId: Int,
        tableId: Int,
        totalPrice: Float,
        priceAfterDiscount: Float,
        isPlacedByStaff: Boolean,
        items: List<OrderItemRequest>
    ): AddResult {
        val now = DbUtcNow()
        val sortedItems = items.sortedBy { it.productId }

        return transaction {
            // Atomische stock check: controleer eerst alle items
            val outOfStock = mutableListOf<String>()
            sortedItems.forEach { item ->
                val stock = Inventory.select { Inventory.productId eq item.productId }
                    .singleOrNull()?.get(Inventory.quantity) ?: 0
                if (stock < item.quantity) {
                    val productName = Product.select { Product.id eq item.productId }
                        .singleOrNull()?.get(Product.name) ?: "Onbekend product"
                    outOfStock.add(productName)
                }
            }
            if (outOfStock.isNotEmpty()) {
                return@transaction AddResult.InsufficientStock(outOfStock)
            }

            // Alles in stock: voeg order in
            val orderId = Order.insert {
                it[Order.userId] = userId
                it[Order.tableId] = tableId
                it[Order.statusId] = 5
                it[Order.totalPrice] = totalPrice
                it[Order.priceAfterDiscount] = priceAfterDiscount
                it[Order.createdAt] = now
                it[Order.isPlacedByStaff] = isPlacedByStaff
                it[Order.remarks] = null
            } get Order.id

            sortedItems.forEach { item ->
                Orderitem.insert {
                    it[Orderitem.orderId] = orderId
                    it[Orderitem.productId] = item.productId
                    it[Orderitem.quantity] = item.quantity
                    it[Orderitem.price] = item.price
                }
                // Update populariteit
                val currentPopularity = Product.select { Product.id eq item.productId }
                    .singleOrNull()?.get(Product.popularity) ?: 0
                Product.update({ Product.id eq item.productId }) {
                    it[Product.popularity] = currentPopularity + item.quantity
                }
                // Verminder stock
                val currentStock = Inventory.select { Inventory.productId eq item.productId }
                    .singleOrNull()?.get(Inventory.quantity) ?: 0
                val newStock = (currentStock - item.quantity).coerceAtLeast(0)
                Inventory.update({ Inventory.productId eq item.productId }) {
                    it[Inventory.quantity] = newStock
                    it[Inventory.lastUpdated] = now
                }
            }

            AddResult.Success(orderId)
        }
    }

    fun updateOrder(
        orderId: Int,
        userId: Int,
        tableId: Int,
        totalPrice: Float,
        priceAfterDiscount: Float,
        items: List<OrderItemRequest>
    ): AddResult = transaction {
        val now = DbUtcNow()
        val sortedItems = items.sortedBy { it.productId }
        val existingOrder = Order.select { Order.id eq orderId }.singleOrNull()
            ?: return@transaction AddResult.InsufficientStock(emptyList())

        if (existingOrder[Order.statusId] == STATUS_BETAALD) {
            return@transaction AddResult.InsufficientStock(listOf("Betaalde bestellingen kunnen niet aangepast worden"))
        }

        val previousItems = Orderitem.select { Orderitem.orderId eq orderId }.toList()
        val prevByProduct = previousItems.groupBy { it[Orderitem.productId] }
            .mapValues { entry -> entry.value.sumOf { row -> row[Orderitem.quantity] } }

        val outOfStock = mutableListOf<String>()
        sortedItems.forEach { item ->
            val currentStock = Inventory.select { Inventory.productId eq item.productId }
                .singleOrNull()?.get(Inventory.quantity) ?: 0
            val previousReserved = prevByProduct[item.productId] ?: 0
            val availableForEdit = currentStock + previousReserved
            if (availableForEdit < item.quantity) {
                val productName = Product.select { Product.id eq item.productId }
                    .singleOrNull()?.get(Product.name) ?: "Onbekend product"
                outOfStock.add(productName)
            }
        }
        if (outOfStock.isNotEmpty()) {
            return@transaction AddResult.InsufficientStock(outOfStock)
        }

        val newByProduct = sortedItems.groupBy { it.productId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
        val allProductIds = (prevByProduct.keys + newByProduct.keys).toSet()

        allProductIds.forEach { productId ->
            val prevQty = prevByProduct[productId] ?: 0
            val newQty = newByProduct[productId] ?: 0
            val delta = newQty - prevQty
            if (delta != 0) {
                val currentStock = Inventory.select { Inventory.productId eq productId }
                    .singleOrNull()?.get(Inventory.quantity) ?: 0
                val updatedStock = (currentStock - delta).coerceAtLeast(0)
                Inventory.update({ Inventory.productId eq productId }) {
                    it[Inventory.quantity] = updatedStock
                    it[Inventory.lastUpdated] = now
                }
                if (delta > 0) {
                    val currentPopularity = Product.select { Product.id eq productId }
                        .singleOrNull()?.get(Product.popularity) ?: 0
                    Product.update({ Product.id eq productId }) {
                        it[Product.popularity] = currentPopularity + delta
                    }
                }
            }
        }

        Orderitem.deleteWhere { SqlExpressionBuilder.run { Orderitem.orderId eq orderId } }
        sortedItems.forEach { item ->
            Orderitem.insert {
                it[Orderitem.orderId] = orderId
                it[Orderitem.productId] = item.productId
                it[Orderitem.quantity] = item.quantity
                it[Orderitem.price] = item.price
            }
        }

        Order.update({ Order.id eq orderId }) {
            it[Order.userId] = userId
            it[Order.tableId] = tableId
            it[Order.totalPrice] = totalPrice
            it[Order.priceAfterDiscount] = priceAfterDiscount
            it[Order.isPlacedByStaff] = existingOrder[Order.isPlacedByStaff]
            it[Order.remarks] = existingOrder[Order.remarks]
        }

        AddResult.Success(orderId)
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

    fun getById(orderId: Int): OrderOverzichtItem? = transaction {
        val orderRow = Order.select { Order.id eq orderId }.singleOrNull() ?: return@transaction null
        buildOrderOverviewItem(orderRow)
    }

    fun getOpenOrdersForClient(clientName: String): ClientOpenOrdersSummary? = transaction {
        val userRow = User.select { User.name eq clientName }.singleOrNull() ?: return@transaction null
        val userId = userRow[User.id]
        val groupId = userRow[User.groupId]
        val discountPct = if (groupId != null) {
            Group.select { Group.id eq groupId }.singleOrNull()?.get(Group.discount) ?: 0f
        } else 0f

        val openOrders = Order.select {
            (Order.userId eq userId) and (Order.statusId neq STATUS_BETAALD)
        }.map { buildOrderOverviewItem(it) }
            .filter { it.statusId in statusFlow && it.statusId != STATUS_BETAALD }

        val totalOpen = openOrders.sumOf { it.totalPrice.toDouble() }.toFloat()
        val totalCheckout = openOrders.filter { it.canCheckout }.sumOf { it.totalPrice.toDouble() }.toFloat()
        val totalOpenAfterDiscount = openOrders.sumOf { (it.priceAfterDiscount ?: it.totalPrice).toDouble() }.toFloat()
        val totalCheckoutAfterDiscount = openOrders.filter { it.canCheckout }.sumOf { (it.priceAfterDiscount ?: it.totalPrice).toDouble() }.toFloat()

        ClientOpenOrdersSummary(
            clientName = clientName,
            orders = openOrders,
            totalOpenAmount = totalOpen,
            totalCheckoutableAmount = totalCheckout,
            totalOpenAmountAfterDiscount = totalOpenAfterDiscount,
            totalCheckoutableAmountAfterDiscount = totalCheckoutAfterDiscount,
            discountPercentage = if (discountPct > 0f) discountPct else null
        )
    }

    fun transitionOrderStatus(orderId: Int, move: String): OrderStatusTransitionResponse = transaction {
        val orderRow = Order.select { Order.id eq orderId }.singleOrNull()
            ?: return@transaction OrderStatusTransitionResponse(success = false, error = "Order niet gevonden")

        val currentStatusId = orderRow[Order.statusId]
        if (currentStatusId == STATUS_BETAALD) {
            return@transaction OrderStatusTransitionResponse(success = false, error = "Betaalde bestellingen kunnen niet meer wijzigen")
        }

        val targetStatusId = when (move.lowercase()) {
            "next" -> nextStatusId(currentStatusId)
            "previous" -> previousStatusId(currentStatusId)
            else -> null
        } ?: return@transaction OrderStatusTransitionResponse(
            success = false,
            error = "Ongeldige statusovergang"
        )

        val updated = Order.update({ Order.id eq orderId }) {
            it[Order.statusId] = targetStatusId
        }

        if (updated <= 0) {
            return@transaction OrderStatusTransitionResponse(success = false, error = "Status kon niet worden aangepast")
        }

        val updatedOrder = Order.select { Order.id eq orderId }.singleOrNull()?.let { buildOrderOverviewItem(it) }
        OrderStatusTransitionResponse(success = updatedOrder != null, order = updatedOrder)
    }

    fun checkoutOrder(orderId: Int): Boolean = transaction {
        val orderRow = Order.select { Order.id eq orderId }.singleOrNull() ?: return@transaction false
        if (orderRow[Order.statusId] != STATUS_AFGELEVERD) {
            return@transaction false
        }
        val updated = Order.update({ Order.id eq orderId }) {
            it[Order.statusId] = STATUS_BETAALD
        }
        updated > 0
    }

    fun checkoutOpenOrdersForClient(clientName: String, fixedDiscountAmount: Float? = null): BulkCheckoutResponse = transaction {
        val summary = getOpenOrdersForClient(clientName)
            ?: return@transaction BulkCheckoutResponse(success = false, error = "Klant niet gevonden")

        val normalizedFixedDiscount = (fixedDiscountAmount ?: 0f).coerceAtLeast(0f)
        val checkoutBaseAmount = summary.totalCheckoutableAmountAfterDiscount ?: summary.totalCheckoutableAmount
        val finalCheckoutAmount = (checkoutBaseAmount - normalizedFixedDiscount).coerceAtLeast(0f)

        val checkoutableOrders = summary.orders.filter { it.statusId == STATUS_AFGELEVERD }
        if (checkoutableOrders.isEmpty()) {
            return@transaction BulkCheckoutResponse(
                success = false,
                summary = summary,
                error = "Geen af te rekenen bestellingen voor deze klant",
                appliedFixedDiscountAmount = normalizedFixedDiscount,
                finalCheckoutAmount = finalCheckoutAmount
            )
        }

        val updatedOrderIds = mutableListOf<Int>()
        checkoutableOrders.forEach { order ->
            val updated = Order.update({ (Order.id eq order.id) and (Order.statusId eq STATUS_AFGELEVERD) }) {
                it[Order.statusId] = STATUS_BETAALD
            }
            if (updated > 0) {
                updatedOrderIds += order.id
            }
        }

        val updatedSummary = getOpenOrdersForClient(clientName)
            ?: ClientOpenOrdersSummary(
                clientName = clientName,
                orders = emptyList(),
                totalOpenAmount = 0f,
                totalCheckoutableAmount = 0f
            )

        BulkCheckoutResponse(
            success = updatedOrderIds.isNotEmpty(),
            summary = updatedSummary,
            updatedOrderIds = updatedOrderIds,
            error = if (updatedOrderIds.isEmpty()) "Geen bestellingen aangepast" else null,
            appliedFixedDiscountAmount = normalizedFixedDiscount,
            finalCheckoutAmount = finalCheckoutAmount
        )
    }

    fun setInBehandeling(orderId: Int): Boolean = transaction {
        transitionOrderStatus(orderId, "next").success
    }

    fun deleteOrder(orderId: Int): DeleteOrderResponse = transaction {
        val now = DbUtcNow()
        val orderRow = Order.select { Order.id eq orderId }.singleOrNull()
            ?: return@transaction DeleteOrderResponse(success = false, error = "Order niet gevonden")

        if (orderRow[Order.statusId] != STATUS_AANGEMAAKT) {
            return@transaction DeleteOrderResponse(success = false, error = "Alleen aangemaakte bestellingen kunnen verwijderd worden")
        }

        val orderItems = Orderitem.select { Orderitem.orderId eq orderId }.toList()
        orderItems.forEach { itemRow ->
            val productId = itemRow[Orderitem.productId]
            val quantity = itemRow[Orderitem.quantity]
            val currentStock = Inventory.select { Inventory.productId eq productId }
                .singleOrNull()?.get(Inventory.quantity) ?: 0
            Inventory.update({ Inventory.productId eq productId }) {
                it[Inventory.quantity] = currentStock + quantity
                it[Inventory.lastUpdated] = now
            }
        }

        Orderitem.deleteWhere { SqlExpressionBuilder.run { Orderitem.orderId eq orderId } }
        val deleted = Order.deleteWhere { SqlExpressionBuilder.run { Order.id eq orderId } }

        if (deleted > 0) {
            DeleteOrderResponse(success = true, deletedOrderId = orderRow[Order.id])
        } else {
            DeleteOrderResponse(success = false, error = "Bestelling kon niet verwijderd worden")
        }
    }
}
