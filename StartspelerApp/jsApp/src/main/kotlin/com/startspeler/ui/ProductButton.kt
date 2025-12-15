package com.startspeler.ui

import react.*
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Typography
import mui.material.styles.TypographyVariant
import kotlin.js.json

external interface ProductButtonProps : Props {
    var label: String
    var price: String
    var onClick: () -> Unit
    var disabled: Boolean?
}

val ProductButton = FC<ProductButtonProps> { props ->
    val bg = if (props.disabled == true) "#bfbfe0" else "#2f2b7b"

    Button {
        disabled = props.disabled ?: false
        variant = ButtonVariant.contained
        onClick = { _ -> if (props.disabled != true) props.onClick() }

        asDynamic().sx = json(
            "width" to 140,
            "height" to 80,
            "borderRadius" to 16,
            "backgroundColor" to bg,
            "color" to "white",
            "textTransform" to "none",
            "display" to "flex",
            "flexDirection" to "column",
            "alignItems" to "center",
            "justifyContent" to "center",
            "gap" to 4,
            "boxShadow" to "none"
        )

        Typography {
            variant = TypographyVariant.h6
            asDynamic().sx = js("({ fontWeight: 700, fontSize: 16 })")
            +props.label
        }

        Typography {
            variant = TypographyVariant.body1
            asDynamic().sx = js("({ fontSize: 14 })")
            +props.price
        }
    }
}
