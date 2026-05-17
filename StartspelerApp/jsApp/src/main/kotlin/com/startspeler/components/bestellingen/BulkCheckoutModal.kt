package com.startspeler.components.bestellingen

import com.startspeler.dto.ClientOpenOrdersSummary
import com.startspeler.util.formatUtcTimestampForDisplay
import mui.material.Box
import mui.material.Button
import mui.material.FormControl
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Modal
import mui.material.Select
import mui.material.TextField
import mui.material.Typography
import react.FC
import react.Props
import react.useEffect
import react.useState

private fun Float.fmt(): String { val n: dynamic = this; return n.toFixed(2) as String }

external interface BulkCheckoutModalProps : Props {
    var open: Boolean
    var summary: ClientOpenOrdersSummary?
    var loading: Boolean
    var error: String?
    var onClose: () -> Unit
    var onConfirm: (Float?) -> Unit
}

val BulkCheckoutModal = FC<BulkCheckoutModalProps> { props ->
    val summary = props.summary
    if (!props.open || summary == null) return@FC
    val shouldScrollOrders = summary.orders.size > 3
    val hasDiscount = (summary.discountPercentage ?: 0f) > 0f
    val (fixedDiscountOption, setFixedDiscountOption) = useState("none")
    val (customDiscountInput, setCustomDiscountInput) = useState("")

    useEffect(dependencies = arrayOf(props.open, summary.clientName)) {
        if (props.open) {
            setFixedDiscountOption("none")
            setCustomDiscountInput("")
        }
    }

    val checkoutBaseAmount = summary.totalCheckoutableAmountAfterDiscount ?: summary.totalCheckoutableAmount
    val normalizedCustomDiscountInput = customDiscountInput.trim().replace(',', '.')
    val parsedCustomDiscount = normalizedCustomDiscountInput.toFloatOrNull()
    val customDiscountError = when {
        fixedDiscountOption != "custom" -> null
        normalizedCustomDiscountInput.isBlank() -> "Voer een positief bedrag in"
        parsedCustomDiscount == null -> "Alleen positieve getallen zijn toegelaten"
        parsedCustomDiscount <= 0f -> "Voer een positief bedrag groter dan 0 in"
        else -> null
    }
    val isCustomDiscountValid = customDiscountError == null

    val selectedFixedDiscountAmount = when (fixedDiscountOption) {
        "10" -> 10f
        "5" -> 5f
        "custom" -> if (isCustomDiscountValid) parsedCustomDiscount!! else 0f
        else -> 0f
    }
    val finalCheckoutAmount = (checkoutBaseAmount - selectedFixedDiscountAmount).coerceAtLeast(0f)

    Modal {
        open = true
        onClose = { _, _ -> props.onClose() }
        Box {
            sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: 'min(640px, 90vw)', background: 'white', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.18)', padding: '20px', outline: 'none', display: 'flex', flexDirection: 'column', gap: '14px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h6
                +"Afrekenen voor ${summary.clientName}"
            }
            Typography {
                sx = js("{ color: '#666', fontSize: '0.92rem' }")
                +"Afgeleverde bestellingen worden afgerekend. Andere openstaande bestellingen blijven zichtbaar."
            }
            Box {
                sx = if (shouldScrollOrders) {
                    js("{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '280px', overflowY: 'auto', background: '#f7f9fc', borderRadius: '8px', padding: '12px' }")
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
                        val formattedDate = formatUtcTimestampForDisplay(order.createdAt)
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
                                Typography { sx = js("{ fontSize: '0.88rem', fontWeight: 600 }"); +(if (order.canCheckout) "Betalen" else order.status) }
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
                Typography {
                    sx = js("{ fontWeight: 700, color: '#1976d2' }")
                    +"Totaal bedrag: € ${checkoutBaseAmount.fmt()}"
                }

                if (hasDiscount) {
                    val checkoutAfterDiscount = summary.totalCheckoutableAmountAfterDiscount ?: summary.totalCheckoutableAmount
                    val checkoutSaving = summary.totalCheckoutableAmount - checkoutAfterDiscount

                    Box {
                        sx = js("{ display: 'flex', justifyContent: 'flex-end', marginTop: '4px' }")
                        Typography {
                            sx = js("{ color: '#e65100', fontWeight: 500, fontSize: '0.88rem', background: '#fff3e0', borderRadius: '6px', padding: '4px 8px' }")
                            +"Groep korting: ${summary.discountPercentage!!.toInt()}%"
                        }
                    }
                    Typography {
                        sx = js("{ fontSize: '0.95rem', color: '#e65100' }")
                        +"Groep korting: - € ${checkoutSaving.fmt()}"
                    }
                }

                Box {
                    sx = js("{ display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap', marginTop: '10px' }")
                    FormControl {
                        sx = js("{ minWidth: '220px' }")
                        size = mui.material.Size.small
                        InputLabel { +"Vaste korting" }
                        Select {
                            value = fixedDiscountOption
                            label = react.ReactNode("Vaste korting")
                            onChange = { event, _ ->
                                setFixedDiscountOption(event.target.value)
                                if (event.target.value != "custom") {
                                    setCustomDiscountInput("")
                                }
                            }
                            MenuItem { value = "none"; +"Geen korting" }
                            MenuItem { value = "10"; +"10 euro" }
                            MenuItem { value = "5"; +"5 euro" }
                            MenuItem { value = "custom"; +"Zelf invullen" }
                        }
                    }
                    if (fixedDiscountOption == "custom") {
                        Box {
                            sx = js("{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }")
                            TextField {
                                label = react.ReactNode("Bedrag")
                                value = customDiscountInput
                                error = customDiscountError != null
                                asDynamic().type = "number"
                                asDynamic().inputProps = js("{ min: '0.01', step: '0.01', inputMode: 'decimal' }")
                                asDynamic().onChange = { event: dynamic -> setCustomDiscountInput(event.target.value as String) }
                                size = mui.material.Size.small
                                sx = js("{ width: '140px' }")
                            }
                            if (customDiscountError != null) {
                                Typography {
                                    sx = js("{ color: '#d32f2f', fontSize: '0.85rem', maxWidth: '220px' }")
                                    +customDiscountError
                                }
                            }
                        }
                    }
                }

                if (selectedFixedDiscountAmount > 0f) {
                    Typography {
                        sx = js("{ fontSize: '0.95rem', color: '#e65100' }")
                        +"Vaste korting: - € ${selectedFixedDiscountAmount.fmt()}"
                    }
                }
                Typography {
                    sx = js("{ fontWeight: 700, color: '#1565c0', fontSize: '1rem' }")
                    +"Te betalen: € ${finalCheckoutAmount.fmt()}"
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
                    disabled = props.loading || summary.orders.none { it.canCheckout } || !isCustomDiscountValid
                    onClick = { props.onConfirm(if (selectedFixedDiscountAmount > 0f) selectedFixedDiscountAmount else null) }
                    +"Afrekenen"
                }
            }
        }
    }
}
