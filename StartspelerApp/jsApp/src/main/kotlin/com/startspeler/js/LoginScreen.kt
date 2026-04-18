package com.startspeler.js

import com.startspeler.ui.LoginPage
import kotlinx.browser.window
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import react.FC
import react.Props
import react.useEffect
import react.useEffectOnce
import react.useState
import kotlin.js.Promise

external interface LoginScreenProps : Props {
    var loggedIn: Boolean
    var setLoggedIn: (Boolean) -> Unit
}

val LoginScreen = FC<LoginScreenProps> { props ->
    var loading by useState(false)
    var error by useState<String?>(null)
    var backendUrl by useState<String?>(null)

    // JWT token state (in-memory for now, but also persisted in localStorage)
    var jwtToken by useState<String?>(null)

    var username by useState("")
    var password by useState("")

    // Sign out function
    val onSignOut: () -> Unit = {
        val baseUrl = backendUrl
        if (baseUrl != null) {
            window.fetch(
                baseUrl.trimEnd('/') + "/logout",
                RequestInit(
                    method = "POST",
                    headers = Headers().apply { set("Content-Type", "application/json") }
                )
            ).then { /* ignore response, just clear state */ }
        }
        jwtToken = null
        window.localStorage.removeItem("jwtToken")
        window.localStorage.removeItem("userRole")
        props.setLoggedIn(false)
        password = ""
    }

    // Function to navigate to Bestel page
    val onGoToBestel = {
        window.location.hash = "#/bestel"
    }

    // Load backendUrl from public/config.json once on mount
    useEffectOnce {
        window
            .fetch("/config.json")
            .then { response ->
                if (!response.ok) {
                    console.error("Failed to load config.json: ", response.status, response.statusText)
                    error = "Failed to load config"
                    return@then
                }
                response.json().then { dataAny ->
                    val data = dataAny.unsafeCast<dynamic>()
                    backendUrl = data.backendUrl as? String
                }
            }
            .catch { throwable ->
                console.error("Error loading config.json", throwable)
                error = "Failed to load config"
            }
    }

    // Clear password whenever loggedIn becomes false (logout or refresh)
    useEffect(dependencies = arrayOf(props.loggedIn)) {
        if (!props.loggedIn) {
            password = ""
        }
    }

    // Sign in function
    val onSignIn: (String, String) -> Unit = { email, pwd ->
        val baseUrl = backendUrl
        if (baseUrl != null) {
            loading = true
            error = null
            val bodyObj = js("({})")
            bodyObj.username = email
            bodyObj.password = pwd
            window
                .fetch(
                    baseUrl.trimEnd('/') + "/login",
                    RequestInit(
                        method = "POST",
                        headers = Headers().apply { set("Content-Type", "application/json") },
                        body = JSON.stringify(bodyObj)
                    )
                )
                .then { response ->
                    if (!response.ok) {
                        error = "Server error: " + response.status.toString() + " " + response.statusText
                        loading = false
                        return@then
                    }
                    response.json().then { dataAny ->
                        val data = dataAny.unsafeCast<dynamic>()
                        console.log("Login response data:", data)
                        if (data.success == true) {
                            val token = data.token as? String
                            val role = data.role as? String
                            console.log("Saving JWT to localStorage:", token)
                            if (token != null) {
                                window.localStorage.setItem("jwtToken", token)
                                jwtToken = token
                            }
                            if (role != null) {
                                window.localStorage.setItem("userRole", role)
                            }
                            props.setLoggedIn(true)
                        } else {
                            error = data.message as? String ?: "Unknown error"
                        }
                        loading = false
                    }
                }
                .catch { _ ->
                    error = "Network error"
                    loading = false
                }
        } else {
            error = "Backend URL not loaded yet"
        }
    }

    // Helper to send JWT in Authorization header for protected requests
    fun authorizedFetch(url: String, init: RequestInit = RequestInit()): Promise<Response> {
        val token = window.localStorage.getItem("jwtToken")
        val headers = init.headers ?: Headers()
        if (token != null && token.isNotBlank()) {
            headers.set("Authorization", "Bearer $token")
        }
        init.headers = headers
        return window.fetch(url, init)
    }

    // Always render LoginPage, let it handle both login and logged-in UI
    LoginPage {
        this.onSignIn = onSignIn
        this.loading = loading
        this.error = error
        this.onGoToBestel = onGoToBestel
        this.loggedIn = props.loggedIn
        this.onSignOut = onSignOut
        this.username = username
        this.setUsername = { username = it }
        this.password = password
        this.setPassword = { password = it }
    }
}
