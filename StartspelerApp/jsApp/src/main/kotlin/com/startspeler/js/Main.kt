package com.startspeler.js

import com.startspeler.ui.BestelPage
import com.startspeler.js.Navbar
import kotlinx.browser.window
import mui.material.CircularProgress
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
            style = js("{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh' }")
            CircularProgress {}
        }
    } else {
        Navbar{
            current = route
            isLoggedIn = loggedIn
        }

        when (route) {
            "login" -> LoginScreen {
                this.loggedIn = loggedIn
                this.setLoggedIn = { loggedIn = it }
            }
            "bestel" -> BestelPage {
                // You can pass loggedIn here if BestelPage needs it
            }
            else -> LoginScreen {
                this.loggedIn = loggedIn
                this.setLoggedIn = { loggedIn = it }
            }
        }
    }
}
