package com.startspeler.js

import kotlinx.browser.window
import mui.material.Box
import mui.material.CircularProgress
import react.FC
import react.Props
import react.create
import react.dom.client.Root
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useEffectOnce
import react.useState
import web.dom.Element
import web.dom.ElementId
import web.dom.document

external fun alert(message: String)

private fun currentRoute(): String {
    val raw = window.location.hash.removePrefix("#").removePrefix("/")
    return if (raw.isEmpty()) "login" else raw.substringBefore("?")
}

private fun queryParam(key: String): String? {
    val hash = window.location.hash.removePrefix("#").removePrefix("/")
    val query = hash.substringAfter("?", "")
    if (query.isEmpty()) return null
    return query.split("&")
        .mapNotNull { part ->
            val kv = part.split("=", limit = 2)
            if (kv.size == 2 && kv[0] == key) kv[1] else null
        }
        .firstOrNull()
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
    var tafelParam by useState(queryParam("tafel"))
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
        window.onhashchange = {
            route = currentRoute()
            tafelParam = queryParam("tafel")
        }
    }

    if (!authChecked) {
        div {
            style =
                js("{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', width: '100vw', margin: 0, padding: 0 }")
            CircularProgress {}
        }
    } else {
        div {
            style = js("{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100vw', margin: 0, padding: 0, overflowX: 'hidden', boxSizing: 'border-box' }")
            Navbar {
                current = route
                isLoggedIn = loggedIn
            }
            // Main content fills the rest
            Box {
                sx = js("{display: 'flex', flexGrow: 1, overflow: 'auto', minWidth: 0 }")
                when {
                    route == "login" -> LoginScreen {
                        this.loggedIn = loggedIn
                        this.setLoggedIn = { loggedIn = it }
                    }
                    route == "bestel" -> BestelScreen {
                        this.initialTafelId = tafelParam?.toIntOrNull()
                    }
                    route == "bestellingen" -> BestellingenScreen {}
                    route == "inventory" -> InventoryScreen {}
                    route == "product" -> ProductScreen {}
                    route == "tables" -> TafelScreen {}
                    route == "klanten" -> KlantenScreen {}
                    route == "groepen" -> GroepenScreen {}
                    route.startsWith("bestel/edit/") -> OrderEditPage {}
                    else -> LoginScreen {
                        this.loggedIn = loggedIn
                        this.setLoggedIn = { loggedIn = it }
                    }
                }
            }
        }
    }
}
