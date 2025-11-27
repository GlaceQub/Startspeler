package com.example.server

import auth.AuthService
import auth.Users
import db.DatabaseFactory
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.io.File

fun main() {
    // Look specifically for the docker-compose .env at {repo_root}/Database/docker/.env,
    // then fall back to a few relative locations. Only .env values are used (no system env).
    fun loadDotenvFromDockerPath(): Dotenv {
        val userDir = System.getProperty("user.dir") ?: "."
        val candidates = listOf(
            // explicit absolute-ish candidate based on current working dir
            File(userDir, "Database/docker").path,
            // common relative locations
            "Database/docker",
            "./Database/docker",
            "../Database/docker",
            "../../Database/docker",
            ".",
            "..",
            "../.."
        )

        for (dir in candidates) {
            val envFile = File(dir, ".env")
            if (envFile.exists()) {
                println("Found .env at: ${envFile.absolutePath}")
                return Dotenv.configure()
                    .directory(dir)
                    .filename(".env")
                    .load()
            }
        }

        println("No .env found in Database/docker or fallbacks; loading default (may be empty)")
        return Dotenv.configure().ignoreIfMissing().load()
    }

    val dotenv = loadDotenvFromDockerPath()

    fun envFromDotenv(key: String, default: String? = null): String? {
        // Dotenv.get may return Any?; coerce to String safely
        val v = dotenv.get(key)
        return if (v == null) default else v.toString()
    }

    // Read MySQL variables commonly used in docker-compose .env files
    val mysqlDatabase = envFromDotenv("MYSQL_DATABASE") ?: run {
        println("MYSQL_DATABASE not found in .env. Please set MYSQL_DATABASE to the database name used by your MySQL container.")
        return
    }
    // Host and port are optional in the .env; default to localhost:3306 so local Docker setups work
    val mysqlHost = envFromDotenv("MYSQL_HOST", "localhost")!!
    val mysqlPort = envFromDotenv("MYSQL_PORT", "3306")!!

    val dbUser = envFromDotenv("MYSQL_USER") ?: run {
        println("MYSQL_USER not found in .env. Please set MYSQL_USER.")
        return
    }

    // Require MYSQL_PASSWORD (do not use root password)
    val dbPass = envFromDotenv("MYSQL_PASSWORD") ?: run {
        println("MYSQL_PASSWORD not found in .env. Please set MYSQL_PASSWORD (do not use MYSQL_ROOT_PASSWORD).")
        return
    }

    // Allow public key retrieval for local dev when using caching_sha2_password auth plugin.
    // NOTE: allowPublicKeyRetrieval=true is fine for local development but not recommended for production without TLS.
    val url = "jdbc:mysql://$mysqlHost:$mysqlPort/$mysqlDatabase?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC"

    val ds = DatabaseFactory.createDataSource(url, dbUser, dbPass)
    DatabaseFactory.connect(ds)

    // Ensure table exists (POC only)
    transaction {
        SchemaUtils.create(Users)
    }

    val auth = AuthService()

    val testUser = envFromDotenv("TEST_USER", "alice")!!
    val testPass = envFromDotenv("TEST_PASS", "password123")!!

    // Insert test user if missing
    transaction {
        val existing = Users.select { Users.username eq testUser }.singleOrNull()
        if (existing == null) {
            val hashed = auth.hashPassword(testPass)
            Users.insert {
                it[Users.username] = testUser
                it[Users.passwordHash] = hashed
            }
            println("Inserted test user '$testUser'.")
        } else {
            println("Test user '$testUser' already exists.")
        }
    }

    // Authenticate
    val ok = auth.authenticate(testUser, testPass)
    println("Authentication for user '$testUser': $ok")
}
