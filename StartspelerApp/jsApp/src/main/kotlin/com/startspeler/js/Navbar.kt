package com.startspeler.js

import kotlinx.browser.window
import react.FC
import react.Props
import mui.material.AppBar
import mui.material.Toolbar
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Typography
import kotlin.js.json

external interface NavBarProps : Props {
    var current: String?
}

val Navbar = FC<NavBarProps> { props ->
    val current = props.current ?: ""

    AppBar {
        // default styling van AppBar; je kunt position attribuut toevoegen indien gewenst
        Toolbar {
            Typography {
                asDynamic().sx = json("flexGrow" to 0, "fontWeight" to 700, "mr" to 2)
                +"Startspeler"
            }

            Button {
                onClick = { _ -> window.location.hash = "#/login" }
                variant = ButtonVariant.contained
                asDynamic().sx = json(
                    "color" to "#000000",
                    "backgroundColor" to if (current == "login") "#e0e0e0" else "#f5f5f5",
                    "textTransform" to "none",
                    "fontWeight" to if (current == "login") 700 else 400,
                    "ml" to 1,
                    "px" to 1
                )
                Typography {
                    +"Login"
                }
            }

            Button {
                onClick = { _ -> window.location.hash = "#/bestel" }
                variant = ButtonVariant.contained
                asDynamic().sx = json(
                    "color" to "#000000",
                    "backgroundColor" to if (current == "bestel") "#e0e0e0" else "#f5f5f5",
                    "textTransform" to "none",
                    "fontWeight" to if (current == "bestel") 700 else 400,
                    "ml" to 1,
                    "px" to 1
                )
                Typography {
                    +"Bestel"
                }
            }
        }
    }
}
