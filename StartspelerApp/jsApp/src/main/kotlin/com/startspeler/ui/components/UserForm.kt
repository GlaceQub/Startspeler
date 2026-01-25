package com.startspeler.ui.components

import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.json
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.button
import react.useEffect
import react.useState
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.Response
import kotlin.js.Promise

@Serializable
data class CreateUserRequest(val username: String, val email: String? = null, val password: String? = null)

@Serializable
data class CreateUserResponse(val id: Int)

/**
 * Optionele props:
 * - mock: wanneer true wordt de request gemockt (default true).
 * - baseUrl: basis-URL van je API (default "http://localhost:8080")
 */
external interface UserFormProps : Props {
    var mock: Boolean?
    var baseUrl: String?
}

val UserForm = FC<UserFormProps> { props ->
    val mockMode = props.mock ?: true
    val baseUrl = props.baseUrl ?: "http://localhost:8080"

    val (username, setUsername) = useState("")
    val (email, setEmail) = useState("")
    val (password, setPassword) = useState("")
    val (loading, setLoading) = useState(false)
    val (message, setMessage) = useState<String?>(null)
    val (isError, setIsError) = useState(false)

    useEffect(username) { setMessage(null); setIsError(false) }
    useEffect(password) { /* clear message on password change if needed */ }

    div {
        asDynamic().className = "user-form-container"
        form {
            asDynamic().className = "user-form"
            onSubmit = { ev ->
                ev.preventDefault()
                setMessage(null)
                setIsError(false)

                val trimmed = username.trim()
                if (trimmed.isEmpty()) {
                    setMessage("Username is required")
                    setIsError(true)
                } else {
                    val dto = CreateUserRequest(
                        username = trimmed,
                        email = email.trim().ifEmpty { null },
                        password = password.trim().ifEmpty { null }
                    )

                    if (mockMode) {
                        // Simuleer latency en responseresult
                        setLoading(true)
                        window.setTimeout({
                            val fakeId = (1000..9999).random()
                            setMessage("User created (id=$fakeId) [mock]")
                            setIsError(false)
                            setUsername("")
                            setEmail("")
                            setPassword("")
                            setLoading(false)
                        }, 600)
                    } else {
                        setLoading(true)
                        val body = Json.encodeToString(dto)
                        val init: dynamic = json(
                            "method" to "POST",
                            "headers" to json("Content-Type" to "application/json"),
                            "body" to body
                        )

                        val fetchPromise: Promise<dynamic> = window.fetch("$baseUrl/api/users", init)

                        fetchPromise.then { respDynamic: dynamic ->
                            val response = respDynamic.unsafeCast<Response>()

                            if (response.ok) {
                                // response.json() heeft type Promise<dynamic>
                                val jsonPromise = response.json().unsafeCast<Promise<dynamic>>()
                                jsonPromise.then { obj: dynamic ->
                                    val idVal = try { (obj.id as? Int) } catch (_: Throwable) { null }
                                    if (idVal != null) {
                                        setMessage("User created (id=$idVal)")
                                    } else {
                                        val text = (obj.message as? String) ?: "User created"
                                        setMessage(text)
                                    }
                                    setIsError(false)
                                    setUsername("")
                                    setEmail("")
                                    setPassword("")
                                    setLoading(false)
                                    null
                                }.catch { err: dynamic ->
                                    setMessage("Error reading response: ${err?.toString() ?: "unknown"}")
                                    setIsError(true)
                                    setLoading(false)
                                    null
                                }
                            } else {
                                val textPromise = response.text().unsafeCast<Promise<String>>()
                                textPromise.then { text: String ->
                                    setMessage("Error ${response.status}: $text")
                                    setIsError(true)
                                    setLoading(false)
                                    null
                                }.catch { err: dynamic ->
                                    setMessage("Error ${response.status}: (failed to read body)")
                                    setIsError(true)
                                    setLoading(false)
                                    null
                                }
                            }
                            null
                        }.catch { err: dynamic ->
                            setMessage("Network error: ${err?.toString() ?: "unknown"}")
                            setIsError(true)
                            setLoading(false)
                            null
                        }
                    }
                }
            }

            label {
                asDynamic().className = "form-row"
                +"Username:"
                input {
                    asDynamic().className = "user-input"
                    name = "username"
                    value = username
                    onChange = { e ->
                        val target = e.target as? HTMLInputElement
                        setUsername(target?.value ?: "")
                    }
                }
            }

            label {
                asDynamic().className = "form-row"
                +"Email (optioneel):"
                input {
                    asDynamic().className = "user-input"
                    name = "email"
                    asDynamic().type = "email"
                    value = email
                    onChange = { e ->
                        val target = e.target as? HTMLInputElement
                        setEmail(target?.value ?: "")
                    }
                }
            }

            label {
                asDynamic().className = "form-row"
                +"Wachtwoord (optioneel):"
                input {
                    asDynamic().className = "user-input"
                    name = "password"
                    asDynamic().type = "password"
                    value = password
                    onChange = { e ->
                        val target = e.target as? HTMLInputElement
                        setPassword(target?.value ?: "")
                    }
                    placeholder = "Laat leeg om geen wachtwoord te zetten"
                }
            }

            button {
                asDynamic().className = "user-submit"
                asDynamic().type = "submit"
                asDynamic().disabled = loading
                if (loading) +"Aanmaken..."
                else +"Maak gebruiker"
            }

            message?.let {
                div {
                    asDynamic().className = if (isError) "feedback error" else "feedback success"
                    +it
                }
            }
        }
    }
}