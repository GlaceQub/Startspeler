package com.startspeler.js

import com.startspeler.ui.BestelPage
import com.startspeler.ui.LoginPage
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.client.Root
import react.useState
import web.dom.document
import web.dom.Element
import web.dom.ElementId

external fun alert(message: String)

fun main() {
    val container: Element? = document.getElementById(ElementId("root"))
    if (container != null) {
        val root: Root = createRoot(container)
        root.render(App.create())
    }
}

//private val App = FC<Props> {
//    LoginPage {
//        onSignIn = { email, password ->
//            alert("Sign in clicked: email=$email, password=$password")
//        }
//        loading = false
//        error = null
//    }
//}
private val App = FC<Props> {
    LoginScreen {}
    /*
    var page by useState("login")

    when (page) {
        "login" -> LoginPage {
            onSignIn = { email, password ->
                alert("Sign in clicked: email=$email, password=$password")
            }
            loading = false
            error = null
            onGoToBestel = { page = "bestel" } // navigate to BestelPage
        }
        "bestel" -> BestelPage { } // render BestelPage
        else -> LoginPage {
            onSignIn = { email, password ->
                alert("Sign in clicked: email=$email, password=$password")
            }
            loading = false
            error = null
            onGoToBestel = { page = "bestel" }
        }
    }
    */
}