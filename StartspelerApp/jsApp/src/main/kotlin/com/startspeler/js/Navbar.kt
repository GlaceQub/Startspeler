package com.startspeler.js

import kotlinx.browser.window
import react.FC
import react.Props
import mui.material.AppBar
import mui.material.Toolbar
import mui.material.Button
import mui.material.ButtonVariant
import react.dom.html.ReactHTML
import kotlin.js.json

external interface NavBarProps : Props {
    var current: String?
    var isLoggedIn: Boolean
}

val Navbar = FC<NavBarProps> { props ->
    val current = props.current ?: ""
    val isLoggedIn = props.isLoggedIn

    AppBar {
        position = mui.material.AppBarPosition.static
        asDynamic().className = "navbar"
        elevation = 0
        asDynamic().sx = json(
            "backgroundColor" to "#2B3078",
            "color" to "#ffffff"
        )

        Toolbar {
            ReactHTML.span{
                asDynamic().className = "logo"

                ReactHTML.img {
                    asDynamic().src = "/images/logostartspeler.png"
                    asDynamic().alt = "Startspeler Logo"
                }
            }

            ReactHTML.span {
                asDynamic().className = "buttons"

                // LOGIN knop
                Button {
                    onClick = { _ -> window.location.hash = "#/login" }
                    variant = ButtonVariant.contained
                    disableElevation = true
                    val cls = if (current == "login") "nav-button active" else "nav-button"
                    asDynamic().className = cls
                    if (current == "login") asDynamic()["aria-current"] = "page"
                    +(if (isLoggedIn) "account" else "Login")
                }

                // BESTEL knop
                Button {
                    onClick = { _ -> window.location.hash = "#/bestel" }
                    variant = ButtonVariant.contained
                    disableElevation = true
                    val cls = if (current == "bestel") "nav-button active" else "nav-button"
                    asDynamic().className = cls
                    if (current == "bestel") asDynamic()["aria-current"] = "page" // Use correct ARIA attribute
                    +"Bestel"
                }

                // KLant aanmaken knop
                Button {
                    onClick = { _ -> window.location.hash = "#/usercreate" }
                    variant = ButtonVariant.contained
                    disableElevation = true
                    val cls = if (current == "usercreate") "nav-button active" else "nav-button"
                    asDynamic().className = cls
                    if (current == "usercreate") asDynamic().ariaCurrent = "page"
                    +"Klant aanmaken"
                }

                // Inventory knop (optioneel, alleen zichtbaar voor ingelogde gebruikers)
                Button {
                    onClick = { _ -> window.location.hash = "/inventory" }
                    variant = ButtonVariant.contained
                    disableElevation = true
                    val cls = if (current == "inventory") "nav-button active" else "nav-button"
                    asDynamic().className = cls
                    if (current == "inventory") asDynamic().ariaCurrent = "page"
                    +"Inventory"
            }
        }
    }
}
}
