package com.example.server

import auth.AuthService
import db.DatabaseFactory
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import db.tables.User
import auth.UserManager
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.*
import com.example.server.routes.authRoutes
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*

fun main() {
    //region Database setup
    /** Look specifically for the docker-compose .env at {repo_root}/Database/docker/.env, */
    fun loadDotenvFromDockerPath(): Dotenv {
        val userDir = System.getProperty("user.dir") ?: "."
        val possibleDirs = listOf(
            File(userDir, "../Database/docker"),
            File(userDir, "../../Database/docker"),
            File(userDir).parentFile?.resolve("Database/docker"),
            File(userDir, "Database/docker")
        )
        for (dir in possibleDirs) {
            if (dir != null) {
                val envFile = File(dir, ".env")
                if (envFile.exists()) {
                    println("Found .env at: ${envFile.absolutePath}")
                    return Dotenv.configure()
                        .directory(dir.absolutePath)
                        .filename(".env")
                        .load()
                }
            }
        }
        error("ERROR: .env file not found in Database/docker. Please create and configure the .env file.")
    }

    val dotenv = loadDotenvFromDockerPath()

    fun envFromDotenv(key: String, default: String? = null): String? {
        // Dotenv.get may return Any?; coerce to String safely
        val v = dotenv.get(key)
        return if (v == null) default else v.toString()
    }

    /** Read MySQL variables commonly used in docker-compose .env files */
    val mysqlDatabase = envFromDotenv("MYSQL_DATABASE") ?: run {
        println("MYSQL_DATABASE not found in .env. Please set MYSQL_DATABASE to the database name used by your MySQL container.")
        return
    }
    /** Host and port are optional in the .env; default to localhost:3306 so local Docker setups work */
    val mysqlHost = envFromDotenv("MYSQL_HOST", "localhost")!!
    val mysqlPort = envFromDotenv("MYSQL_PORT", "3306")!!

    val dbUser = envFromDotenv("MYSQL_USER") ?: run {
        println("MYSQL_USER not found in .env. Please set MYSQL_USER.")
        return
    }

    /** Require MYSQL_PASSWORD (do not use root password) */
    val dbPass = envFromDotenv("MYSQL_PASSWORD") ?: run {
        println("MYSQL_PASSWORD not found in .env. Please set MYSQL_PASSWORD (do not use MYSQL_ROOT_PASSWORD).")
        return
    }

    /** Allow public key retrieval for local dev when using caching_sha2_password auth plugin.
        NOTE: allowPublicKeyRetrieval=true is fine for local development but not recommended for production without TLS. */
    val url = "jdbc:mysql://$mysqlHost:$mysqlPort/$mysqlDatabase?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC"

    val ds = DatabaseFactory.createDataSource(url, dbUser, dbPass)
    DatabaseFactory.connect(ds)

    /** Ensure table exists (POC only) */
    transaction {
        SchemaUtils.create(User)
    }
    //endregion

    //region Authentication
    val auth = AuthService()

    /** Check if default user exists or is created */
    UserManager.createDefaultAdminUser()

    // Check if admin user exists after creation
    val adminUser = UserManager.getUserByName("admin")
    println("Admin user exists: ${adminUser != null}")

    // Try authenticating the admin user
    val authResult = auth.authenticate("admin", "St4rtsp3l3r")
    println("Authentication for user 'admin' with default password: $authResult")
    //endregion

    //region Ktor HTTP server
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CORS) {
            anyHost()
            allowHeader("Content-Type")
        }
        routing {
            intercept(ApplicationCallPipeline.Call) {
                try {
                    proceed()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    throw t
                }
            }

            // Simple health endpoint to verify server + DB
            get("/health") {
                call.respondText("OK")
            }

            authRoutes(auth)
        }
    }.start(wait = true)
    //endregion
}
