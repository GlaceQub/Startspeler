package com.startspeler.ui

import com.startspeler.ui.components.UserForm
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

val UserCreateScreen = FC<Props> {
    div {
        asDynamic().className = "usercreate-screen"
        UserForm {
            baseUrl = "http://localhost:8080"
        }
    }
}