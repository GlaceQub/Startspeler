package startspeler.server

import db.DatabaseFactory
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import db.tables.User
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import startspeler.server.routes.authRoutes
import startspeler.server.routes.registerUserRoutes
import startspeler.server.routes.categoryRoutes
import startspeler.server.routes.productRoutes
import startspeler.server.routes.orderRoutes
import startspeler.server.routes.klantenRoutes
import startspeler.server.routes.tafelRoutes
import startspeler.server.routes.inventoryRoutes
import startspeler.server.routes.groupRoutes
import startspeler.server.repository.UserRepository
import io.ktor.http.HttpMethod

fun main() {
    //region Database setup
    /**
     * Searches for a .env file in the following locations (first match wins):
     *  1. server/  directory  – convenient when running locally without Docker
     *  2. {repo_root}/Database/docker/  – legacy Docker-compose location
     *
     * For a Combell-hosted DB, place your .env in either location and set
     * MYSQL_HOST to the hostname provided by Combell.
     */
    fun loadDotenvFromDockerPath(): Dotenv {
        val userDir = System.getProperty("user.dir") ?: "."
        val possibleDirs = listOf(
            // server/ directory (run from StartspelerApp or server/)
            File(userDir, "server"),
            File(userDir),
            // legacy Database/docker locations
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
        error(
            "ERROR: .env file not found. " +
            "Create a .env file in the 'server/' directory (or Database/docker/) " +
            "with MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER and MYSQL_PASSWORD."
        )
    }

    val dotenv = loadDotenvFromDockerPath()

    fun envFromDotenv(key: String, default: String? = null): String? {
        // Dotenv.get may return Any?; coerce to String safely
        val v = dotenv[key]
        return v ?: default
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

    /** Build the JDBC connection URL.
     *  - For a Combell hosted DB, SSL is typically required; set useSSL=true and
     *    allowPublicKeyRetrieval=true so the driver can retrieve the server's public key over SSL.
     *  - If you are still running a local (non-Docker) MySQL without SSL, you can
     *    set MYSQL_USE_SSL=false in your .env to override.
     */
    val useSSL = envFromDotenv("MYSQL_USE_SSL", "true")!!
    val url = "jdbc:mysql://$mysqlHost:$mysqlPort/$mysqlDatabase" +
        "?allowPublicKeyRetrieval=true" +
        "&useSSL=$useSSL" +
        "&connectionTimeZone=UTC" +
        "&forceConnectionTimeZoneToSession=true"

    val ds = DatabaseFactory.createDataSource(url, dbUser, dbPass)
    DatabaseFactory.connect(ds)

    /** Ensure table exists (POC only) */
    transaction {
        SchemaUtils.create(User)
    }
    //endregion

    //region Authentication
    // Ensure default admin user exists (moved from previous AuthService logic)
    UserRepository.createDefaultAdminUser()
    val adminUser = UserRepository.getUserByName("admin")
    println("Admin user exists: ${adminUser != null}")
    //endregion

    //region JWT config
    val jwtSecret = envFromDotenv("JWT_SECRET") ?: "dev-secret-key" // Use a strong secret in production!
    val jwtIssuer = "startspeler-app"
    val jwtAudience = "startspeler-users"
    val jwtRealm = "startspeler-realm"
    val algorithm = Algorithm.HMAC256(jwtSecret)
    //endregion

    //region Ktor HTTP server
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CORS) {
            anyHost()
            allowHeader("Content-Type")
            allowHeader("Authorization")
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }
        install(Authentication) {
            jwt("auth-jwt") {
                realm = jwtRealm
                verifier(
                    JWT
                        .require(algorithm)
                        .withAudience(jwtAudience)
                        .withIssuer(jwtIssuer)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.getClaim("username").asString().isNotEmpty()) JWTPrincipal(credential.payload) else null
                }
            }
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

            registerUserRoutes()
            // Delegate all auth routes to routes/AuthRoutes.kt
            authRoutes(
                jwtIssuer,
                jwtAudience,
                jwtRealm,
                algorithm
            )
            // Register category routes
            categoryRoutes()
            // Register product routes
            productRoutes()
            // Register order routes
            orderRoutes()
            // Register klanten and tafels routes
            klantenRoutes()
            tafelRoutes()
            inventoryRoutes()
            groupRoutes()
        }
    }.start(wait = true)
    //endregion
}
