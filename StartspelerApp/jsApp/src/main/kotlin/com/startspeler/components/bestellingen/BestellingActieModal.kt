package com.startspeler.components.bestellingen

import mui.material.Box
import mui.material.Modal
import mui.material.Typography
import mui.material.Button as MuiButton
import react.FC
import react.Props

external interface BestellingActieModalProps : Props {
    var open: Boolean
    var title: String
    var description: String
    var confirmLabel: String
    var loading: Boolean
    var error: String?
    var onClose: () -> Unit
    var onConfirm: () -> Unit
}

val BestellingActieModal = FC<BestellingActieModalProps> { props ->
    if (!props.open) return@FC

    Modal {
        open = true
        onClose = { _, _ -> props.onClose() }
        // Modal expects exactly ONE child
        Box {
            sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', minWidth: '320px', background: '#fff', borderRadius: '8px', boxShadow: '0 8px 32px rgba(0,0,0,0.18)', padding: '32px', outline: 'none', display: 'flex', flexDirection: 'column', gap: '16px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h6
                +props.title
            }
            Typography {
                variant = mui.material.styles.TypographyVariant.body1
                +props.description
            }
            if (props.error != null) {
                Typography {
                    sx = js("{ color: 'red' }")
                    +props.error!!
                }
            }
            Box {
                sx = js("{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }")
                MuiButton {
                    variant = mui.material.ButtonVariant.outlined
                    onClick = { props.onClose() }
                    +"Annuleren"
                }
                MuiButton {
                    variant = mui.material.ButtonVariant.contained
                    color = mui.material.ButtonColor.primary
                    disabled = props.loading
                    onClick = { props.onConfirm() }
                    +props.confirmLabel
                }
            }
        }
    }
}
