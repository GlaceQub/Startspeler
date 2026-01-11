package com.startspeler.js

import kotlinx.browser.window
import mui.material.CircularProgress
import mui.material.Toolbar
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.client.Root
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useEffectOnce
import react.useState
import web.dom.document
import web.dom.Element
import web.dom.ElementId

external fun alert(message: String)

private fun currentRoute(): String {
    val raw = window.location.hash.removePrefix("#").removePrefix("/")
    return if (raw.isEmpty()) "login" else raw
}

fun main() {
    val container: Element? = document.getElementById(ElementId("root"))
    if (container != null) {
        val root: Root = createRoot(container)
        root.render(App.create())
    } else {
        error("Add a `div#root` to your index.html")
    }
}

private val App = FC<Props> {
    var route by useState(currentRoute())
    var loggedIn by useState(false)
    var authChecked by useState(false)

    // On mount, check for JWT in localStorage and set loggedIn if present
    useEffectOnce {
        val storedToken = window.localStorage.getItem("jwtToken")
        if (storedToken != null && storedToken.isNotBlank()) {
            loggedIn = true
        }
        authChecked = true
    }

    // alleen side-effect: luister naar hash changes en cleanup terugzetten
    useEffect(emptyList<Unit>()) {
        val prev = window.onhashchange
        window.onhashchange = {
            route = currentRoute()
        }
    }

    if (!authChecked) {
        div {
            style = js("{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', width: '100vw', margin: 0, padding: 0 }")
            CircularProgress {}
        }
    } else {
        div {
            style = js("{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100vw', margin: 0, padding: 0 }")
            Navbar{
                current = route
                isLoggedIn = loggedIn
            }
            // Toolbar spacer if needed for fixed navbar (not static)
            // Toolbar { asDynamic().className = "navbar-spacer" }
            // Main content fills the rest
            when (route) {
                "login" -> LoginScreen {
                    this.loggedIn = loggedIn
                    this.setLoggedIn = { loggedIn = it }
                }
                "bestel" -> BestelScreen {
                    // You can pass loggedIn here if BestelScreen needs it
                }
                else -> LoginScreen {
                    this.loggedIn = loggedIn
                    this.setLoggedIn = { loggedIn = it }
                }
            }
        }
    }
}
