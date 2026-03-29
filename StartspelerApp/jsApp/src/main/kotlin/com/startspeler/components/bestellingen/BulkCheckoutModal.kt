package com.startspeler.components.bestellingen

import com.startspeler.dto.ClientOpenOrdersSummary
import mui.material.Box
import mui.material.Button
import mui.material.Modal
import mui.material.Typography
import react.FC
import react.Props

external interface BulkCheckoutModalProps : Props {
    var open: Boolean
    var summary: ClientOpenOrdersSummary?
    var loading: Boolean
    var error: String?
    var onClose: () -> Unit
    var onConfirm: () -> Unit
}

val BulkCheckoutModal = FC<BulkCheckoutModalProps> { props ->
    val summary = props.summary
    if (!props.open || summary == null) return@FC

    Modal {
        open = true
        onClose = { _, _ -> props.onClose() }
        Box {
            sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: 'min(680px, 92vw)', background: 'white', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.18)', padding: '24px', outline: 'none', display: 'flex', flexDirection: 'column', gap: '16px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h6
                +"Openstaande rekeningen van ${summary.clientName}"
            }
            Typography {
                sx = js("{ color: '#666', fontSize: '0.92rem' }")
                +"Alle afgeleverde bestellingen hieronder kunnen in één keer afgerekend worden. Andere openstaande bestellingen blijven zichtbaar maar worden nog niet afgerekend."
            }
            Box {
                sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '320px', overflowY: 'auto', background: '#f7f9fc', borderRadius: '8px', padding: '12px' }")
                if (summary.orders.isEmpty()) {
                    Typography { +"Geen openstaande bestellingen gevonden." }
                } else {
                    summary.orders.forEach { order ->
                        Box {
                            sx = js("{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0', borderBottom: '1px solid #e8edf3' }")
                            Typography { +"#${order.id} · Tafel ${order.tableNumber} · ${order.status}" }
                            Typography { +"€ ${order.totalPrice}" }
                        }
                    }
                }
            }
            Box {
                sx = js("{ display: 'flex', flexDirection: 'column', gap: '4px' }")
                Typography {
                    sx = js("{ fontWeight: 700, color: '#1976d2' }")
                    +"Totaal openstaand: € ${summary.totalOpenAmount}"
                }
                Typography {
                    sx = js("{ fontWeight: 600, color: '#2e7d32', fontSize: '0.95rem' }")
                    +"Nu afrekenbaar: € ${summary.totalCheckoutableAmount}"
                }
            }
            if (props.error != null) {
                Typography {
                    sx = js("{ color: '#d32f2f', fontSize: '0.9rem' }")
                    +props.error!!
                }
            }
            Box {
                sx = js("{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }")
                Button {
                    variant = mui.material.ButtonVariant.outlined
                    onClick = { props.onClose() }
                    +"Sluiten"
                }
                Button {
                    variant = mui.material.ButtonVariant.contained
                    color = mui.material.ButtonColor.primary
                    disabled = props.loading || summary.orders.none { it.statusId == 6 }
                    onClick = { props.onConfirm() }
                    +"Alles afrekenen"
                }
            }
        }
    }
}
