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
    val shouldScrollOrders = summary.orders.size > 3

    Modal {
        open = true
        onClose = { _, _ -> props.onClose() }
        Box {
            sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: 'min(720px, 92vw)', background: 'white', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.18)', padding: '24px', outline: 'none', display: 'flex', flexDirection: 'column', gap: '16px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h6
                +"Openstaande rekeningen van ${summary.clientName}"
            }
            Typography {
                sx = js("{ color: '#666', fontSize: '0.92rem' }")
                +"Afgeleverde bestellingen worden afgerekend. Andere openstaande bestellingen blijven zichtbaar in een lichtere stijl."
            }
            Box {
                sx = if (shouldScrollOrders) {
                    js("{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '340px', overflowY: 'auto', background: '#f7f9fc', borderRadius: '8px', padding: '12px' }")
                } else {
                    js("{ display: 'flex', flexDirection: 'column', gap: '8px', background: '#f7f9fc', borderRadius: '8px', padding: '12px' }")
                }
                if (summary.orders.isEmpty()) {
                    Typography { +"Geen openstaande bestellingen gevonden." }
                } else {
                    summary.orders.forEach { order ->
                        val isCheckoutable = order.canCheckout
                        val bgColor = if (isCheckoutable) "#e8f5e9" else "#f3f4f6"
                        val textColor = if (isCheckoutable) "#1b5e20" else "#6b7280"
                        val formattedDate = order.createdAt?.let {
                            try {
                                val parts = it.split("T", limit = 2)
                                if (parts.size == 2) {
                                    val date = parts[0].split("-")
                                    val time = parts[1].substring(0, 5)
                                    if (date.size == 3) "$time ${date[2]}-${date[1]}-${date[0]}" else it
                                } else it
                            } catch (_: Exception) { it }
                        } ?: "-"
                        val rowSx = js("({})")
                        rowSx.display = "flex"
                        rowSx.justifyContent = "space-between"
                        rowSx.alignItems = "center"
                        rowSx.padding = "10px 12px"
                        rowSx.borderRadius = "8px"
                        rowSx.background = bgColor
                        rowSx.color = textColor
                        Box {
                            sx = rowSx
                            Box {
                                sx = js("{ display: 'flex', flexDirection: 'column', gap: '2px' }")
                                Typography { +"#${order.id} · Tafel ${order.tableNumber}" }
                                Typography { sx = js("{ fontSize: '0.88rem' }"); +formattedDate }
                                Typography { sx = js("{ fontSize: '0.88rem', fontWeight: 600 }"); +order.status }
                            }
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
                    disabled = props.loading || summary.orders.none { it.canCheckout }
                    onClick = { props.onConfirm() }
                    +"Alles afrekenen"
                }
            }
        }
    }
}
