package com.startspeler.components.bestellingen

import com.startspeler.dto.OrderOverzichtItem
import mui.material.Box
import mui.material.Modal
import mui.material.Typography
import mui.material.Button as MuiButton
import react.FC
import react.Props

external interface AfrekenModalProps : Props {
    var open: Boolean
    var order: OrderOverzichtItem?
    var loading: Boolean
    var error: String?
    var onClose: () -> Unit
    var onConfirm: () -> Unit
}

val AfrekenModal = FC<AfrekenModalProps> { props ->
    val order = props.asDynamic().order as? OrderOverzichtItem
    if (!props.open || order == null) return@FC

    Modal {
        open = props.open
        onClose = { _, _ -> props.onClose() }
        // Modal expects exactly ONE child
        Box {
            sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', minWidth: '320px', background: '#fff', borderRadius: '8px', boxShadow: '0 8px 32px rgba(0,0,0,0.18)', padding: '32px', outline: 'none', display: 'flex', flexDirection: 'column', gap: '16px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h6
                +"Bent u zeker dat u deze bestelling wilt afrekenen?"
            }
            Typography {
                variant = mui.material.styles.TypographyVariant.body1
                +"Totale prijs: € ${order.totalPrice}"
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
                    +"Bevestigen"
                }
            }
        }
    }
}
