package startspeler.server.repository

import com.startspeler.dto.ProductItem
import com.startspeler.models.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction
import utils.DbUtcNow
import db.tables.Product as ProductTable
import db.tables.Inventory as InventoryTable

object ProductRepository {
    fun getAll(): List<Product> = transaction {
        ProductTable.selectAll().map {
            Product(
                id = it[ProductTable.id],
                name = it[ProductTable.name],
                categoryId = it[ProductTable.categoryId],
                price = it[ProductTable.price],
                popularity = it[ProductTable.popularity]
            )
        }
    }

    fun getByCategoryId(categoryId: Int): List<Product> = transaction {
        ProductTable.select { ProductTable.categoryId eq categoryId }.map {
            Product(
                id = it[ProductTable.id],
                name = it[ProductTable.name],
                categoryId = it[ProductTable.categoryId],
                price = it[ProductTable.price],
                popularity = it[ProductTable.popularity]
            )
        }
    }

    fun getAllByCategoryWithStock(categoryId: Int): List<ProductItem> = transaction {
        (ProductTable innerJoin InventoryTable).select { ProductTable.categoryId eq categoryId }.map { row ->
            val quantity = row.getOrNull(InventoryTable.quantity) ?: 0
            ProductItem(
                id = row[ProductTable.id],
                name = row[ProductTable.name],
                price = row[ProductTable.price],
                outOfStock = quantity <= 0,
                stockQuantity = quantity
            )
        }
    }

    fun getById(id: Int): Product? = transaction {
        ProductTable.select { ProductTable.id eq id }.map {
            Product(
                id = it[ProductTable.id],
                name = it[ProductTable.name],
                categoryId = it[ProductTable.categoryId],
                price = it[ProductTable.price],
                popularity = it[ProductTable.popularity]
            )
        }.singleOrNull()
    }

    fun getTopPopularity(limit: Int = 3): List<Product> = transaction {
        ProductTable.selectAll().orderBy(ProductTable.popularity, SortOrder.DESC).limit(limit).map {
            Product(
                id = it[ProductTable.id],
                name = it[ProductTable.name],
                categoryId = it[ProductTable.categoryId],
                price = it[ProductTable.price],
                popularity = it[ProductTable.popularity]
            )
        }
    }

    fun getTopPopularityCategory(categoryId: Int, limit: Int = 3): List<Product> = transaction {
        ProductTable.select { ProductTable.categoryId eq categoryId }.orderBy(ProductTable.popularity, SortOrder.DESC)
            .limit(limit).map {
                Product(
                    id = it[ProductTable.id],
                    name = it[ProductTable.name],
                    categoryId = it[ProductTable.categoryId],
                    price = it[ProductTable.price],
                    popularity = it[ProductTable.popularity]
                )
            }
    }

    private fun validateProductInput(name: String, price: Float) {
        require(name.isNotBlank()) { "Product naam is verplicht" }
        require(price >= 0) { "Prijs mag niet negatief zijn" }
    }
    fun create(name: String, categoryId: Int, price: Float, popularity: Int = 0): Product = transaction {
        val stmt = ProductTable.insert { row ->
            row[ProductTable.name] = name
            row[ProductTable.categoryId] = categoryId
            row[ProductTable.price] = price
            row[ProductTable.popularity] = popularity
        }

        val newId = stmt[ProductTable.id]

        InventoryTable.insert { row ->
            row[InventoryTable.productId] = newId
            row[InventoryTable.quantity] = 0
            row[InventoryTable.minimumQuantity] = 24
            row[InventoryTable.lastUpdated] = DbUtcNow()
        }

        Product(
            id = newId,
            name = name,
            categoryId = categoryId,
            price = price,
            popularity = popularity
        )
    }

    fun update(id: Int, name: String, categoryId: Int, price: Float, popularity: Int = 0): Product? = transaction {
        val updated = ProductTable.update({ ProductTable.id eq id }) { row ->
            row[ProductTable.name] = name
            row[ProductTable.categoryId] = categoryId
            row[ProductTable.price] = price
            row[ProductTable.popularity] = popularity
        }
        if (updated == 0) null
        else Product(id = id, name = name, categoryId = categoryId, price = price, popularity = popularity)
    }

    fun delete(id: Int): Boolean = transaction {
        ProductTable.deleteWhere { ProductTable.id eq id } > 0
    }

    fun existsByNameAndCategory(name: String, categoryId: Int, excludeId: Int? = null): Boolean = transaction {
        val base = (ProductTable.name eq name) and (ProductTable.categoryId eq categoryId)
        val cond = if (excludeId == null) base else base and (ProductTable.id neq excludeId)
        ProductTable.select { cond }.limit(1).any()
    }

}
