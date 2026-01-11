package com.startspeler.ui

import react.*
import mui.material.Box
import mui.material.Typography
import mui.material.TextField
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.styles.TypographyVariant
import react.dom.onChange
import web.html.InputType
import web.html.password


external interface LoginPageProps : Props {
    var onSignIn: (username: String, password: String) -> Unit
    var loading: Boolean
    var error: String?
    var onGoToBestel: (() -> Unit)? // navigation callback
    var loggedIn: Boolean
    var onSignOut: (() -> Unit)?
    var username: String
    var setUsername: (String) -> Unit
    var password: String
    var setPassword: (String) -> Unit
}

val LoginPage = FC<LoginPageProps> { props ->
    Box {
        sx = js("({ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' })")
        // Centered Card for login content
        mui.material.Card {
            sx = js("({px: 3,py: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', boxShadow: 3, width: 300, borderRadius: 2 })")
            Typography {
                sx = js("({ marginBottom: 4 })")
                variant = TypographyVariant.h4
                +"Startspeler - Login"
            }
            if (props.loggedIn) {
                Typography {
                    variant = TypographyVariant.h5
                    +"Welcome! You are now logged in."
                }
                props.onSignOut?.let { onSignOut ->
                    Button {
                        sx = js("({ marginTop: 4 })")
                        onClick = { onSignOut() }
                        +"Logout"
                    }
                }
            } else {
                TextField {
                    label = ReactNode("Username")
                    value = props.username
                    placeholder = "username"
                    onChange = { event ->
                        props.setUsername(event.target.asDynamic().value as String)
                    }
                    fullWidth = true
                    sx = js("({ marginBottom: 3 })")
                }
                TextField {
                    label = ReactNode("Password")
                    value = props.password
                    onChange = { event ->
                        props.setPassword(event.target.asDynamic().value as String)
                    }
                    type = InputType.password
                    fullWidth = true
                    sx = js("({ marginBottom: 2 })")
                }
                Button {
                    variant = ButtonVariant.contained
                    onClick = { _ -> props.onSignIn(props.username, props.password) }
                    sx = js("({ width: '100%' })")
                    +(if (props.loading) "Signing in..." else "Sign In")
                }
                props.error?.let { err ->
                    Typography {
                        this.asDynamic().color = "error"
                        sx = js("({ marginTop: 6 })")
                        +(err.toString())
                    }
                }
            }
        }
    }
}
