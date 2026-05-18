package com.startspeler.ui

import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import react.FC
import react.Props

external interface PageTitleBarProps : Props {
    var title: String
    var rightContent: (() -> Unit)?
}

val PageTitleBar = FC<PageTitleBarProps> { props ->
    Box {
        sx = js("{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }")

        Typography {
            variant = TypographyVariant.h5
            sx = js("{ color: '#2B3078', fontWeight: 700 }")
            +props.title
        }

        Box {
            props.rightContent?.invoke()
        }
    }
}