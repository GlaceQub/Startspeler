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
            Box {
                sx = js("{ display: 'flex', flexDirection: 'row', gap: '16px', alignItems: 'center', width: '100%' }")
                Typography { variant = mui.material.styles.TypographyVariant.body2; sx = js("{ width: '80px', minWidth: '80px', maxWidth: '80px', overflow: 'hidden', textOverflow: 'ellipsis' }"); +"${order.id}" }
                Typography { variant = mui.material.styles.TypographyVariant.body2; sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis' }"); +"Tafel ${order.tableNumber}" }
                Typography { variant = mui.material.styles.TypographyVariant.body2; sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis' }"); +order.clientName }
                Typography { variant = mui.material.styles.TypographyVariant.body2; sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis' }"); +order.status }
                Typography {
                    variant = mui.material.styles.TypographyVariant.body2
                    sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis' }")
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
                    sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: '16px', paddingTop: '12px', borderTop: '2px solid #1976d2', width: 'calc(100% - 24px)', marginLeft: '12px' }")
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
                            mui.material.Button {
                                variant = mui.material.ButtonVariant.outlined
                                color = mui.material.ButtonColor.primary
                                size = mui.material.Size.small
                                onClick = {
                                    setStatusDirection("previous")
                                    setShowStatusModal(true)
                                }
                                +order.previousStatusLabel!!
                            }
                        }
                        if (order.canGoToNextStatus && order.nextStatusLabel != null) {
                            mui.material.Button {
                                variant = mui.material.ButtonVariant.contained
                                color = if (order.canCheckout) mui.material.ButtonColor.primary else mui.material.ButtonColor.secondary
                                size = mui.material.Size.small
                                onClick = {
                                    if (order.canCheckout) {
                                        props.onCheckout?.invoke()
                                    } else {
                                        setStatusDirection("next")
                                        setShowStatusModal(true)
                                    }
                                }
                                +(if (order.canCheckout) "Betalen" else order.nextStatusLabel!!)
                            }
                        }
                        mui.material.Button {
                            variant = mui.material.ButtonVariant.outlined
                            color = mui.material.ButtonColor.primary
                            size = mui.material.Size.small
                            disabled = !order.canEdit
                            onClick = {
                                if (order.canEdit) {
                                    kotlinx.browser.window.location.hash = "#/bestel/edit/${order.id}"
                                }
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