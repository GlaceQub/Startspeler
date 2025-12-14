package com.startspeler.js

import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.client.Root
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

private val App = FC<Props> {
    LoginScreen {}
}