package com.startspeler.components.bestellingen

import com.startspeler.dto.ClientOpenOrdersSummary
import mui.material.Box
import mui.material.Button
import mui.material.Modal
import mui.material.Typography
import react.FC
import react.Props

private fun Float.fmt(): String { val n: dynamic = this; return n.toFixed(2) as String }

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
    val hasDiscount = (summary.discountPercentage ?: 0f) > 0f

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
                            // Price column
                            val orderDiscount = order.discountPercentage
                            val orderAfterDiscount = order.priceAfterDiscount ?: order.totalPrice
                            if (isCheckoutable && orderDiscount != null && orderDiscount > 0f) {
                                Box {
                                    sx = js("{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '1px' }")
                                    Typography {
                                        sx = js("{ fontSize: '0.85rem', textDecoration: 'line-through', color: '#999' }")
                                        +"€ ${order.totalPrice.fmt()}"
                                    }
                                    Typography {
                                        sx = js("{ fontSize: '0.8rem', color: '#e65100' }")
                                        +"- € ${(order.totalPrice - orderAfterDiscount).fmt()}"
                                    }
                                    Typography {
                                        sx = js("{ fontWeight: 700, color: '#1b5e20' }")
                                        +"€ ${orderAfterDiscount.fmt()}"
                                    }
                                }
                            } else {
                                Typography { +"€ ${order.totalPrice.fmt()}" }
                            }
                        }
                    }
                }
            }
            // Totals section
            Box {
                sx = js("{ display: 'flex', flexDirection: 'column', gap: '6px', borderTop: '2px solid #1976d2', paddingTop: '12px' }")
                if (hasDiscount) {
                    val totalAfterDiscount = summary.totalOpenAmountAfterDiscount ?: summary.totalOpenAmount
                    val checkoutAfterDiscount = summary.totalCheckoutableAmountAfterDiscount ?: summary.totalCheckoutableAmount
                    val totalSaving = summary.totalOpenAmount - totalAfterDiscount
                    val checkoutSaving = summary.totalCheckoutableAmount - checkoutAfterDiscount

                    // Community korting badge just above the breakdown
                    Typography {
                        sx = js("{ color: '#e65100', fontWeight: 500, fontSize: '0.88rem', background: '#fff3e0', borderRadius: '6px', padding: '4px 8px', alignSelf: 'flex-start' }")
                        +"Community korting: ${summary.discountPercentage!!.toInt()}%"
                    }

                    // Afrekenbaar breakdown
                    Typography {
                        sx = js("{ fontSize: '0.95rem', color: '#444', marginTop: '4px' }")
                        +"Nu afrekenbaar: € ${summary.totalCheckoutableAmount.fmt()}"
                    }
                    if (checkoutSaving > 0f) {
                        Typography {
                            sx = js("{ fontSize: '0.95rem', color: '#e65100' }")
                            +"Korting (${summary.discountPercentage!!.toInt()}%): - € ${checkoutSaving.fmt()}"
                        }
                    }
                    Box { sx = js("{ borderTop: '1px solid #bdbdbd', marginTop: '2px', marginBottom: '2px' }") }
                    Typography {
                        sx = js("{ fontWeight: 700, color: '#2e7d32', fontSize: '1rem' }")
                        +"Resterend te betalen: € ${checkoutAfterDiscount.fmt()}"
                    }
                } else {
                    Typography {
                        sx = js("{ fontWeight: 700, color: '#1976d2' }")
                        +"Totaal openstaand: € ${summary.totalOpenAmount.fmt()}"
                    }
                    Typography {
                        sx = js("{ fontWeight: 600, color: '#2e7d32', fontSize: '0.95rem' }")
                        +"Nu afrekenbaar: € ${summary.totalCheckoutableAmount.fmt()}"
                    }
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
