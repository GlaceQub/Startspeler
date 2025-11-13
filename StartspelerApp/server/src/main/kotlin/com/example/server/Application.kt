package com.example.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*

fun main() {
    embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
        install(CORS) {
            allowHost("localhost:3000")
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
        }
        routing {
            get("/health") {
                call.respondText("OK")
            }
            post("/orders") {
                val order = call.receiveText()
                // TODO: validate JWT, store order to DB
                call.respondText("Order received: $order")
            }
        }
    }.start(wait = true)
}

