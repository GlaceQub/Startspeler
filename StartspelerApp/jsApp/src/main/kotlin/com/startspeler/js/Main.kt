package com.startspeler.js

import com.startspeler.ui.BestelPage
import com.startspeler.ui.LoginPage import com.startspeler.js.Navbar
import kotlinx.browser.window
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.client.Root
import react.useEffect
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

    // alleen side-effect: luister naar hash changes en cleanup terugzetten
    useEffect(emptyList<Unit>()) {
        val prev = window.onhashchange
        window.onhashchange = {
            route = currentRoute()
        }

    }

    Navbar{
        current = route
    }

    when (route) {
        "login" -> LoginPage {
            onSignIn = { email, password ->
                alert("Sign in clicked: email=$email, password=$password")
            }
            loading = false
            error = null
            onGoToBestel = { window.location.hash = "#/bestel" } // navigate to BestelPage
        }
        "bestel" -> BestelPage { } // render BestelPage
        else -> LoginPage {
            onSignIn = { email, password ->
                alert("Sign in clicked: email=$email, password=$password")
            }
            loading = false
            error = null
            onGoToBestel = { window.location.hash = "#/bestel" }
        }
    }
}
