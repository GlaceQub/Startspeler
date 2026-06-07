package com.startspeler.components.bestellingen

import com.startspeler.dto.OrderOverzichtItem
import com.startspeler.util.formatUtcTimestampForDisplay
import mui.icons.material.ExpandMore
import mui.material.Accordion
import mui.material.AccordionDetails
import mui.material.AccordionSummary
import mui.material.Box
import mui.material.Typography
import react.FC
import react.Props
import react.create
import react.useState

private fun Float.fmt(): String { val n: dynamic = this; return n.toFixed(2) as String }

private fun statusColour(status: String?) = when (status?.lowercase()?.trim()) {
    "aangemaakt"     -> "#1976d2"  // blue
    "in behandeling" -> "#ed6c02"  // orange
    "afgeleverd"     -> "#2e7d32"  // green
    "betaald"        -> "#00695c"  // teal
    else             -> "#555555"
}

external interface OrderOverzichtItemProps : Props {
    var order: OrderOverzichtItem
    var isOpen: Boolean
    var onCheckout: (() -> Unit)?
    var onDelete: (() -> Unit)?
    var canDelete: Boolean
    var onMoveToNextStatus: (() -> Unit)?
    var onMoveToPreviousStatus: (() -> Unit)?
}

val OrderOverzichtItem = FC<OrderOverzichtItemProps> { props ->
    val order = props.order
    val (isOpen, setOpen) = useState(props.isOpen)
    val (showStatusModal, setShowStatusModal) = useState(false)
    val (statusDirection, setStatusDirection) = useState<String?>(null)
    val (showDeleteModal, setShowDeleteModal) = useState(false)
    val canRenderDelete = props.canDelete && order.canDelete

    val discountPct = order.discountPercentage
    val afterDiscount = order.priceAfterDiscount ?: order.totalPrice

    Accordion {
        expanded = isOpen
        onChange = { _, expanded -> setOpen(expanded) }
        AccordionSummary {
            expandIcon = ExpandMore.create()
            asDynamic().sx = js("{ minHeight: '48px', '&.Mui-expanded': { minHeight: '48px' }, padding: '0 12px' }")
            Box {
                sx = js("{ display: 'flex', flexDirection: 'row', gap: '16px', alignItems: 'center', width: '100%', padding: '2px 0' }")
                Typography { sx = js("{ width: '80px', minWidth: '80px', maxWidth: '80px', overflow: 'hidden', textOverflow: 'ellipsis', fontSize: '0.92rem', fontFamily: '\"Roboto\", system-ui, sans-serif' }"); +"${order.id}" }
                Typography { sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', fontSize: '0.92rem', fontFamily: '\"Roboto\", system-ui, sans-serif' }"); +"Tafel ${order.tableNumber}" }
                Typography { sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis', fontSize: '0.92rem', fontFamily: '\"Roboto\", system-ui, sans-serif' }"); +order.clientName }
                // Status shown as a coloured pill
                Box {
                    val c = statusColour(order.status)
                    val pillSx: dynamic = js("({})")
                    pillSx.width = "130px"
                    pillSx.minWidth = "130px"
                    pillSx.maxWidth = "130px"
                    pillSx.display = "inline-flex"
                    pillSx.alignItems = "center"
                    pillSx.justifyContent = "center"
                    pillSx.borderRadius = "10px"
                    pillSx.padding = "4px 10px"
                    pillSx.backgroundColor = c
                    pillSx.color = "#fff"
                    pillSx.fontSize = "0.92rem"     // matches other columns
                    pillSx.fontWeight = 600
                    pillSx.fontFamily = "\"Roboto\", system-ui, -apple-system, \"Segoe UI\", \"Helvetica Neue\", Arial, sans-serif"
                    pillSx.lineHeight = "1.4"       // matches MUI Typography default
                    sx = pillSx
                    +order.status
                }
                Typography {
                    sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis', fontSize: '0.92rem', fontFamily: '\"Roboto\", system-ui, sans-serif' }")
                    +formatUtcTimestampForDisplay(order.createdAt, emptyFallback = "")
                }
            }
        }
        AccordionDetails {
            sx = js("{ padding: '0' }")
            Box {
                sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '8px', background: '#f5f7fa', borderRadius: '0 0 8px 8px' }")
                order.orderitems.forEach { item ->
                    Typography {
                        sx = js("{ fontSize: '0.9rem' }")
                        +"${item.product} x ${item.quantity} (€ ${item.price.fmt()})"
                    }
                }
                Box {
                    sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: '16px', paddingTop: '12px', borderTop: '1px solid #1976d2', width: 'calc(100% - 24px)', marginLeft: '12px' }")
                    Box {
                        sx = js("{ display: 'flex', flexDirection: 'column', gap: '2px' }")
                        if (discountPct != null && discountPct > 0f) {
                            Typography {
                                sx = js("{ fontSize: '0.95rem', color: '#444' }")
                                +"Totaal bedrag: € ${order.totalPrice.fmt()}"
                            }
                            Typography {
                                sx = js("{ fontSize: '0.95rem', color: '#e65100' }")
                                +"Korting (${discountPct.toInt()}%): - € ${(order.totalPrice - afterDiscount).fmt()}"
                            }
                            Box { sx = js("{ borderTop: '1px solid #bdbdbd', marginTop: '4px', marginBottom: '4px' }") }
                            Typography {
                                sx = js("{ fontWeight: 700, fontSize: '1rem', color: '#1976d2' }")
                                +"Resterend te betalen: € ${afterDiscount.fmt()}"
                            }
                        } else {
                            Typography {
                                sx = js("{ fontWeight: 600, fontSize: '1rem', color: '#1976d2' }")
                                +"Totaal: € ${order.totalPrice.fmt()}"
                            }
                        }
                    }
                    Box {
                        sx = js("{ display: 'flex', gap: '8px', flexWrap: 'wrap', justifyContent: 'flex-end' }")
                        if (order.canGoToPreviousStatus && order.previousStatusLabel != null) {
                            val c = statusColour(order.previousStatusLabel)
                            val bSx: dynamic = js("({})")
                            bSx.borderColor = c; bSx.color = c
                            mui.material.Button {
                                variant = mui.material.ButtonVariant.outlined
                                size = mui.material.Size.small
                                sx = bSx
                                onClick = { setStatusDirection("previous"); setShowStatusModal(true) }
                                +order.previousStatusLabel!!
                            }
                        }
                        if (order.canGoToNextStatus && order.nextStatusLabel != null) {
                            val label = if (order.canCheckout) "Betalen" else order.nextStatusLabel!!
                            val c = if (order.canCheckout) statusColour("betaald") else statusColour(order.nextStatusLabel)
                            val bSx: dynamic = js("({})")
                            bSx.backgroundColor = c; bSx.color = "#fff"; bSx.borderColor = c
                            mui.material.Button {
                                variant = mui.material.ButtonVariant.contained
                                size = mui.material.Size.small
                                sx = bSx
                                onClick = {
                                    if (order.canCheckout) props.onCheckout?.invoke()
                                    else { setStatusDirection("next"); setShowStatusModal(true) }
                                }
                                +label
                            }
                        }
                        mui.material.Button {
                            variant = mui.material.ButtonVariant.outlined
                            color = mui.material.ButtonColor.primary
                            size = mui.material.Size.small
                            disabled = !order.canEdit
                            onClick = {
                                if (order.canEdit) kotlinx.browser.window.location.hash = "#/bestel/edit/${order.id}"
                            }
                            +"Aanpassen"
                        }
                        if (canRenderDelete) {
                            mui.material.Button {
                                variant = mui.material.ButtonVariant.outlined
                                color = mui.material.ButtonColor.error
                                size = mui.material.Size.small
                                onClick = { setShowDeleteModal(true) }
                                +"Verwijderen"
                            }
                        }
                    }
                }
            }
        }

        if (canRenderDelete) {
            BestellingActieModal {
                open = showDeleteModal
                title = "Bent u zeker dat u deze bestelling wilt verwijderen?"
                description = "De bestelling wordt verwijderd en de stock wordt terug aangepast."
                confirmLabel = "Verwijderen"
                loading = false
                error = null
                onClose = { setShowDeleteModal(false) }
                onConfirm = {
                    setShowDeleteModal(false)
                    props.onDelete?.invoke()
                }
            }
        }

        if (showStatusModal && statusDirection != null) {
            val targetLabel = if (statusDirection == "previous") order.previousStatusLabel else order.nextStatusLabel
            BestellingActieModal {
                open = true
                title = "Status wijzigen?"
                description = "Nieuwe status: ${targetLabel ?: "onbekend"}"
                confirmLabel = "Bevestigen"
                loading = false
                error = null
                onClose = {
                    setShowStatusModal(false)
                    setStatusDirection(null)
                }
                onConfirm = {
                    setShowStatusModal(false)
                    val direction = statusDirection
                    setStatusDirection(null)
                    if (direction == "previous") props.onMoveToPreviousStatus?.invoke() else props.onMoveToNextStatus?.invoke()
                }
            }
        }
    }
}