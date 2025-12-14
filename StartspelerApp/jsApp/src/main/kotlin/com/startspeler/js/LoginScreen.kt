package com.startspeler.js

import com.startspeler.ui.LoginPage
import kotlinx.browser.window
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import react.FC
import react.Props
import react.useEffectOnce
import react.useState
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant

val LoginScreen = FC<Props> {
    var loading by useState(false)
    var error by useState<String?>(null)
    var loggedIn by useState(false)
    var backendUrl by useState<String?>(null)

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

    if (loggedIn) {
        // Simple Welcome screen after login
        Box {
            sx = js("({ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh' })")
            Typography {
                variant = TypographyVariant.h3
                +"Welcome! You are now logged in."
            }
        }
    } else {
        LoginPage {
            onSignIn = { email, password ->
                val baseUrl = backendUrl

                if (baseUrl != null) {
                    loading = true
                    error = null

                    val bodyObj = js("({})")
                    bodyObj.username = email
                    bodyObj.password = password

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
                                // Non-2xx HTTP status: show a clear server error
                                error = "Server error: " + response.status.toString() + " " + response.statusText
                                loading = false
                                return@then
                            }

                            response.json().then { dataAny ->
                                val data = dataAny.unsafeCast<dynamic>()
                                if (data.success == true) {
                                    loggedIn = true
                                } else {
                                    error = data.message as? String ?: "Unknown error"
                                }
                                loading = false
                            }
                        }
                        .catch { _ ->
                            // True network failures (connection refused, CORS block, etc.)
                            error = "Network error"
                            loading = false
                        }
                } else {
                    error = "Backend URL not loaded yet"
                }
            }
            this.loading = loading
            this.error = error
        }
    }
}
