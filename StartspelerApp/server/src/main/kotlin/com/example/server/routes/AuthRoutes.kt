package com.example.server.routes

import auth.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.authRoutes(auth: AuthService) {
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
                call.respond(LoginResponse(success = true, message = "Login successful"))
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
}

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val success: Boolean, val message: String)
