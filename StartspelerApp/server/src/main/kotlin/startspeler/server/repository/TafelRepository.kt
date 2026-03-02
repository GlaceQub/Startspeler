package com.startspeler.server.repository

import com.startspeler.models.TableModel
import db.tables.Status as StatusTable
import db.tables.TableModel as TablesTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

// zelfde exceptions-stijl als bij producten
class TableAlreadyExistsException(message: String) : RuntimeException(message)
class TableNotFoundException(message: String) : RuntimeException(message)

object TafelRepository {

    fun getAll(): List<TableModel> = transaction {
        (TablesTable innerJoin StatusTable)
            .selectAll()
            .orderBy(TablesTable.number, SortOrder.ASC)
            .map { row ->
                TableModel(
                    id = row[TablesTable.id],
                    number = row[TablesTable.number],
                    statusId = row[TablesTable.statusId],
                    statusName = row[StatusTable.name]
                )
            }
    }

    fun getById(id: Int): TableModel? = transaction {
        (TablesTable innerJoin StatusTable)
            .select { TablesTable.id eq id }
            .map { row ->
                TableModel(
                    id = row[TablesTable.id],
                    number = row[TablesTable.number],
                    statusId = row[TablesTable.statusId],
                    statusName = row[StatusTable.name]
                )
            }
            .singleOrNull()
    }

    fun create(number: Int, statusId: Int): TableModel = transaction {
        if (existsByNumber(number)) throw TableAlreadyExistsException("Tafel $number bestaat al.")

        val stmt = TablesTable.insert { row ->
            row[TablesTable.number] = number
            row[TablesTable.statusId] = statusId
        }
        val newId = stmt[TablesTable.id]

        // return met statusName (join)
        (TablesTable innerJoin StatusTable)
            .select { TablesTable.id eq newId }
            .map { row ->
                TableModel(
                    id = row[TablesTable.id],
                    number = row[TablesTable.number],
                    statusId = row[TablesTable.statusId],
                    statusName = row[StatusTable.name]
                )
            }
            .single()
    }

    fun updateStatus(id: Int, statusId: Int): TableModel? = transaction {
        val updated = TablesTable.update({ TablesTable.id eq id }) { row ->
            row[TablesTable.statusId] = statusId
        }
        if (updated == 0) null
        else {
            (TablesTable innerJoin StatusTable)
                .select { TablesTable.id eq id }
                .map { row ->
                    TableModel(
                        id = row[TablesTable.id],
                        number = row[TablesTable.number],
                        statusId = row[TablesTable.statusId],
                        statusName = row[StatusTable.name]
                    )
                }
                .single()
        }
    }

    fun delete(id: Int): Boolean = transaction {
        TablesTable.deleteWhere { TablesTable.id eq id } > 0
    }

    fun existsByNumber(number: Int, excludeId: Int? = null): Boolean = transaction {
        val cond = if (excludeId == null) {
            TablesTable.number eq number
        } else {
            (TablesTable.number eq number) and (TablesTable.id neq excludeId)
        }
        TablesTable.select { cond }.limit(1).any()
    }
}