package com.startspeler.js

import kotlinx.browser.window
import react.FC
import react.Props
import react.useState
import mui.material.AppBar
import mui.material.Toolbar
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.IconButton
import mui.material.Drawer
import mui.material.DrawerAnchor
import mui.material.List
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.Divider
import mui.icons.material.Menu as MenuIcon
import mui.icons.material.Close as CloseIcon
import mui.system.Box
import react.dom.html.ReactHTML
import kotlin.js.json

external interface NavBarProps : Props {
    var current: String?
    var isLoggedIn: Boolean
}

val Navbar = FC<NavBarProps> { props ->
    val current = props.current ?: ""
    val isLoggedIn = props.isLoggedIn
    val (drawerOpen, setDrawerOpen) = useState(false)

    // Nav items: label, hash, key, loginRequired
    data class NavItem(val label: String, val hash: String, val key: String, val loginRequired: Boolean, val showInMobile: Boolean = false)
    val navItems = listOf(
        NavItem(if (isLoggedIn) "Profiel" else "Login", "#/login", "login", loginRequired = false, showInMobile = true),
        NavItem("Bestel", "#/bestel", "bestel", loginRequired = false, showInMobile = true),
        NavItem("Klanten", "#/klanten", "klanten", true),
        NavItem("Voorraad", "/inventory", "inventory", true),
        NavItem("Communities", "#/groepen", "groepen", true),
        NavItem("Bestellingen", "#/bestellingen", "bestellingen", true),
        NavItem("Tafels", "/tables", "tables", true),
    ).filter { !it.loginRequired || isLoggedIn }

    AppBar {
        position = mui.material.AppBarPosition.static
        asDynamic().className = "navbar"
        elevation = 0
        asDynamic().sx = json(
            "backgroundColor" to "#2B3078",
            "color" to "#ffffff"
        )

        Toolbar {
            ReactHTML.span {
                asDynamic().className = "logo"
                ReactHTML.img {
                    asDynamic().src = "/images/logostartspeler.png"
                    asDynamic().alt = "Startspeler Logo"
                }
            }

            // Desktop buttons – hidden on mobile
            Box {
                sx = js("({ display: { xs: 'none', sm: 'flex' }, gap: '8px' })")
                asDynamic().className = "buttons"
                navItems.forEach { item ->
                    Button {
                        onClick = { _ -> window.location.hash = item.hash }
                        variant = ButtonVariant.contained
                        disableElevation = true
                        val cls = if (current == item.key) "nav-button active" else "nav-button"
                        asDynamic().className = cls
                        if (current == item.key) asDynamic()["aria-current"] = "page"
                        +item.label
                    }
                }
            }

            // Hamburger icon – shown only on mobile
            Box {
                sx = js("({ display: { xs: 'flex', sm: 'none' }, marginLeft: 'auto' })")
                IconButton {
                    asDynamic().color = "inherit"
                    onClick = { _ -> setDrawerOpen(true) }
                    if (drawerOpen) CloseIcon {} else MenuIcon {}
                }
            }
        }
    }

    // Mobile drawer
    Drawer {
        anchor = DrawerAnchor.top
        open = drawerOpen
        onClose = { _, _ -> setDrawerOpen(false) }
        Box {
            sx = js("{ width: '100%', paddingTop: '16px' }")
            asDynamic().role = "presentation"
            List {
                navItems.forEach { item ->
                    if (item.showInMobile) {
                        ListItemButton {
                            selected = current == item.key
                            onClick = {
                                window.location.hash = item.hash
                                setDrawerOpen(false)
                            }
                            ListItemText { primary = react.ReactNode(item.label) }
                        }
                        Divider {}
                    }
                }
            }
        }
    }
}