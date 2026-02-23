package com.startspeler.components.bestellingen

import com.startspeler.dto.OrderOverzichtItem
import mui.material.Accordion
import mui.material.AccordionDetails
import mui.material.AccordionSummary
import mui.material.Box
import mui.material.Typography
import mui.icons.material.ExpandMore
import react.FC
import react.Props
import react.create
import react.useState

external interface OrderOverzichtItemProps : Props {
    var order: OrderOverzichtItem
    var isOpen: Boolean
    var onCheckout: (() -> Unit)?
    var onSetInBehandeling: (() -> Unit)? // Added for status change
}

val OrderOverzichtItem = FC<OrderOverzichtItemProps> { props ->
    val order = props.order
    val (isOpen, setOpen) = useState(props.isOpen)
    val (showBehandelingModal, setShowBehandelingModal) = useState(false)
    val (showCheckoutModal, setShowCheckoutModal) = useState(false)

    Accordion {
        expanded = isOpen
        onChange = { _, expanded -> setOpen(expanded) }
        AccordionSummary {
            expandIcon = ExpandMore.create()
            Box {
                sx = js("{ display: 'flex', flexDirection: 'row', gap: '16px', alignItems: 'center', width: '100%' }")
                Typography {
                    variant = mui.material.styles.TypographyVariant.body1
                    sx = js("{ width: '80px', minWidth: '80px', maxWidth: '80px', overflow: 'hidden', textOverflow: 'ellipsis' }")
                    +"${order.id}"
                }
                Typography {
                    variant = mui.material.styles.TypographyVariant.body1
                    sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis' }")
                    +"Tafel ${order.tableNumber}"
                }
                Typography {
                    variant = mui.material.styles.TypographyVariant.body1
                    sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis' }")
                    +order.clientName
                }
                Typography {
                    variant = mui.material.styles.TypographyVariant.body1
                    sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis' }")
                    +order.status
                }
                Typography {
                    variant = mui.material.styles.TypographyVariant.subtitle1
                    sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis' }")
                    val rawDate = order.createdAt
                    val formatted = rawDate?.let {
                        try {
                            val parts = it.split("T", limit = 2)
                            if (parts.size == 2) {
                                val date = parts[0].split("-")
                                val time = parts[1].substring(0,5)
                                if (date.size == 3) "$time ${date[2]}-${date[1]}-${date[0]}" else it
                            } else it
                        } catch (e: Exception) { it }
                    } ?: ""
                    +formatted
                }
            }
        }
        AccordionDetails {
            sx = js("{ padding: '0' }")
            Box {
                sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '8px', background: '#f5f7fa', borderRadius: '0 0 8px 8px' }")
                // Bestelde producten
                order.orderitems.forEach { item ->
                    Typography {
                        sx = js("{ fontSize: '0.95rem' }")
                        +"${item.product} x ${item.quantity} (€ ${item.price})"
                    }
                }
                // Top border en totaal/knoppen rij
                Box {
                    sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: '16px', paddingTop: '12px', borderTop: '2px solid #1976d2', width: 'calc(100% - 24px)', marginLeft: '12px' }") // Startspeler blauw, border niet helemaal tot rand
                    Typography {
                        sx = js("{ fontWeight: 600, fontSize: '1.05rem', color: '#1976d2' }")
                        +"Totaal: € ${order.totalPrice}"
                    }
                    Box {
                        sx = js("{ display: 'flex', gap: '8px' }")
                        mui.material.Button {
                            variant = mui.material.ButtonVariant.contained
                            color = mui.material.ButtonColor.primary
                            size = mui.material.Size.small
                            disabled = order.status == "betaald"
                            onClick = { setShowCheckoutModal(true) }
                            +"Afrekenen"
                        }
                        mui.material.Button {
                            variant = mui.material.ButtonVariant.outlined
                            color = mui.material.ButtonColor.primary
                            size = mui.material.Size.small
                            onClick = { /* Aanpassen logica */ }
                            +"Aanpassen"
                        }
                        mui.material.Button {
                            variant = mui.material.ButtonVariant.contained
                            color = mui.material.ButtonColor.secondary
                            size = mui.material.Size.small
                            disabled = order.status == "in behandeling" || order.status == "betaald"
                            onClick = { setShowBehandelingModal(true) }
                            +"In behandeling zetten"
                        }
                    }
                }
            }
        }
        // Checkout bevestigingsmodal
        if (showCheckoutModal) {
            mui.material.Modal {
                open = true
                onClose = { _, _ -> setShowCheckoutModal(false) }
                Box {
                    sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: 'white', padding: '24px', borderRadius: '8px', boxShadow: 24, minWidth: '320px' }")
                    Typography {
                        sx = js("{ fontWeight: 600, fontSize: '1.1rem', marginBottom: '12px' }")
                        +"Bent u zeker dat u deze bestelling wilt afrekenen?"
                    }
                    Typography {
                        sx = js("{ marginBottom: '16px' }")
                        +"Totale prijs: € ${order.totalPrice}"
                    }
                    Box {
                        sx = js("{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }")
                        mui.material.Button {
                            onClick = { setShowCheckoutModal(false) }
                            +"Annuleren"
                        }
                        mui.material.Button {
                            variant = mui.material.ButtonVariant.contained
                            color = mui.material.ButtonColor.primary
                            onClick = {
                                setShowCheckoutModal(false)
                                if (!order.status.equals("betaald", ignoreCase = true)) {
                                    props.onCheckout?.invoke()
                                }
                            }
                            +"Bevestigen"
                        }
                    }
                }
            }
        }
        // In behandeling bevestigingsmodal
        if (showBehandelingModal) {
            mui.material.Modal {
                open = true
                onClose = { _, _ -> setShowBehandelingModal(false) }
                Box {
                    sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: 'white', padding: '24px', borderRadius: '8px', boxShadow: 24, minWidth: '320px' }")
                    Typography {
                        sx = js("{ fontWeight: 600, fontSize: '1.1rem', marginBottom: '12px' }")
                        +"Bent u zeker dat u deze bestelling op 'in behandeling' wilt zetten?"
                    }
                    Typography {
                        sx = js("{ marginBottom: '16px' }")
                        +"Status wordt: in behandeling"
                    }
                    Box {
                        sx = js("{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }")
                        mui.material.Button {
                            onClick = { setShowBehandelingModal(false) }
                            +"Annuleren"
                        }
                        mui.material.Button {
                            variant = mui.material.ButtonVariant.contained
                            color = mui.material.ButtonColor.secondary
                            onClick = {
                                setShowBehandelingModal(false)
                                props.onSetInBehandeling?.invoke()
                            }
                            +"Bevestigen"
                        }
                    }
                }
            }
        }
    }
}