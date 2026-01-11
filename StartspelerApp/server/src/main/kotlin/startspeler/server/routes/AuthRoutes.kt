package startspeler.server.routes

import auth.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun Route.authRoutes(
    auth: AuthService,
    jwtIssuer: String,
    jwtAudience: String,
    jwtRealm: String,
    jwtAlgorithm: Algorithm
) {
    post("/login") {
        // Safely parse JSON body into LoginRequest
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                LoginResponse(
                    success = false,
                    message = "Invalid request body"
                )
            )
            return@post
        }

        // Wrap authentication in try/catch so we never leak a raw 500 without JSON
        try {
            val ok = auth.authenticate(loginRequest.username, loginRequest.password)
            if (ok) {
                val token = JWT.create()
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .withClaim("username", loginRequest.username)
                    .sign(jwtAlgorithm)
                call.respond(
                    LoginResponse(
                        success = true,
                        message = "Login successful",
                        token = token
                    )
                )
            } else {
                call.respond(LoginResponse(success = false, message = "Invalid credentials"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                LoginResponse(
                    success = false,
                    message = "Internal server error during authentication"
                )
            )
        }
    }

    post("/logout") {
        // For JWT, logout is handled client-side by deleting the token.
        call.respond(
            LoginResponse(
                success = true,
                message = "Logged out successfully"
            )
        )
    }

    authenticate("auth-jwt") {
        get("/me") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("username").asString()
            call.respond(mapOf("username" to username))
        }
    }
}

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val success: Boolean, val message: String, val token: String? = null)
