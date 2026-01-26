package com.startspeler.ui.components

import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.json
import react.FC
import react.Props
import react.useEffect
import react.useState
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.Response
import kotlin.js.Promise
import mui.material.Box
import mui.material.Button
import mui.material.CircularProgress
import mui.material.TextField
import mui.material.Typography
import mui.material.Alert
import mui.material.Stack

@Serializable
data class CreateUserRequest(val username: String, val email: String? = null)

@Serializable
data class CreateUserResponse(val id: Int)

external interface UserFormProps : Props {
    var baseUrl: String?
}

val UserForm = FC<UserFormProps> { props ->
    val baseUrl = props.baseUrl ?: "http://localhost:8080"

    val (username, setUsername) = useState("")
    val (email, setEmail) = useState("")
    val (loading, setLoading) = useState(false)
    val (message, setMessage) = useState<String?>(null)
    val (isError, setIsError) = useState(false)

    useEffect(username) { setMessage(null); setIsError(false) }

    Box {
        asDynamic().className = "user-form-container"

        Box {
            asDynamic().component = "form"
            asDynamic().className = "user-form"
            asDynamic().onSubmit = { ev: dynamic ->
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
                        email = email.trim().ifEmpty { null }
                    )

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

            Stack {
                asDynamic().spacing = 2
                asDynamic().width = "100%"

                Typography {
                    asDynamic().variant = "h5"
                    +"Maak nieuwe gebruiker"
                }

                TextField {
                    asDynamic().label = "Username"
                    asDynamic().variant = "outlined"
                    asDynamic().fullWidth = true
                    asDynamic().value = username
                    asDynamic().onChange = { e: dynamic ->
                        val v = (e.target as? HTMLInputElement)?.value ?: ""
                        setUsername(v)
                    }
                }

                TextField {
                    asDynamic().label = "Email (optioneel)"
                    asDynamic().variant = "outlined"
                    asDynamic().type = "email"
                    asDynamic().fullWidth = true
                    asDynamic().value = email
                    asDynamic().onChange = { e: dynamic ->
                        val v = (e.target as? HTMLInputElement)?.value ?: ""
                        setEmail(v)
                    }
                }

                if (message != null) {
                    Alert {
                        asDynamic().severity = if (isError) "error" else "success"
                        +message!!
                    }
                }

                Box {
                    asDynamic().display = "flex"
                    asDynamic().alignItems = "center"
                    asDynamic().gap = "12px"

                    Button {
                        asDynamic().variant = "contained"
                        asDynamic().color = "primary"
                        asDynamic().type = "submit"
                        asDynamic().disabled = loading
                        if (loading) {
                            CircularProgress {
                                asDynamic().size = 20
                                asDynamic().color = "inherit"
                            }
                        } else {
                            +"Maak gebruiker"
                        }
                    }
                }
            }
        }
    }
}