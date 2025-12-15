package com.startspeler.ui

import react.*
import mui.material.Box
import mui.material.Typography
import mui.material.TextField
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.styles.TypographyVariant
import react.dom.html.ReactHTML.div
import react.dom.onChange
import web.html.InputType
import web.html.password


external interface LoginPageProps : Props {
    var onSignIn: (username: String, password: String) -> Unit
    var loading: Boolean
    var error: String?
    var onGoToBestel: (() -> Unit)? // navigation callback
}

val LoginPage = FC<LoginPageProps> { props ->
    var username by useState("")
    var password by useState("")

    Box {
        sx = js("({ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', gap: 5 })")

        Typography {
            sx = js("({ marginBottom: 20 })")
            variant = TypographyVariant.h2
            +"Startspeler - Login"
        }

        TextField {
            label = ReactNode("Username")
            value = username
            placeholder = "username"
            onChange = { event ->
                username = event.target.asDynamic().value as String
            }
            fullWidth = true
            sx = js("({ width: '300px' })")
        }

        TextField {
            label = ReactNode("Password")
            value = password
            onChange = { event ->
                password = event.target.asDynamic().value as String
            }
            type = InputType.password
            fullWidth = true
            sx = js("({ width: '300px' })")
        }

        Button {
            variant = ButtonVariant.contained
            onClick = { _ -> props.onSignIn(username, password) }
            +(if (props.loading) "Signing in..." else "Sign In")
        }

        props.error?.let { err ->
            Typography {
                this.asDynamic().color = "error"
                +(err.toString()) // Ensure error is always rendered as a string
            }
        }
    }

    Box {
        sx = js("({ position: 'absolute', bottom: 20, textAlign: 'center', width: '100%' })")

        props.onGoToBestel?.let { goToBestel ->
            Button {
                variant = ButtonVariant.text
                onClick = { _ -> goToBestel() }
                +"Go to Bestel"
            }
        }

    }
}
