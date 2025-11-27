package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun createDataSource(url: String, user: String, password: String): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            driverClassName = "com.mysql.cj.jdbc.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTestQuery = "SELECT 1"
        }
        return HikariDataSource(config)
    }

    fun connect(dataSource: HikariDataSource) {
        Database.connect(dataSource)
    }
}
