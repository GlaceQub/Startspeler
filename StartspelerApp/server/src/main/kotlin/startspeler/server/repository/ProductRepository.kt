package startspeler.server.repository

import com.startspeler.dto.ProductItem
import com.startspeler.models.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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

}
