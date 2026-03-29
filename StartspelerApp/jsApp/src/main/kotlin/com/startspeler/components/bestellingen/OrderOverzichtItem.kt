package com.startspeler.components.bestellingen

import com.startspeler.dto.OrderOverzichtItem
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
    val (showCheckoutModal, setShowCheckoutModal) = useState(false)
    val (showDeleteModal, setShowDeleteModal) = useState(false)
    val canRenderDelete = props.canDelete && order.canDelete

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
                    val rawDate = order.createdAt
                    val formatted = rawDate?.let {
                        try {
                            val parts = it.split("T", limit = 2)
                            if (parts.size == 2) {
                                val date = parts[0].split("-")
                                val time = parts[1].substring(0, 5)
                                if (date.size == 3) "$time ${date[2]}-${date[1]}-${date[0]}" else it
                            } else it
                        } catch (_: Exception) { it }
                    } ?: ""
                    +formatted
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
                        +"${item.product} x ${item.quantity} (€ ${item.price})"
                    }
                }
                Box {
                    sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: '16px', paddingTop: '12px', borderTop: '2px solid #1976d2', width: 'calc(100% - 24px)', marginLeft: '12px' }")
                    Typography {
                        sx = js("{ fontWeight: 600, fontSize: '1rem', color: '#1976d2' }")
                        +"Totaal: € ${order.totalPrice}"
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
                                variant = if (order.canCheckout) mui.material.ButtonVariant.contained else mui.material.ButtonVariant.contained
                                color = if (order.canCheckout) mui.material.ButtonColor.primary else mui.material.ButtonColor.secondary
                                size = mui.material.Size.small
                                onClick = {
                                    if (order.canCheckout) {
                                        setShowCheckoutModal(true)
                                    } else {
                                        setStatusDirection("next")
                                        setShowStatusModal(true)
                                    }
                                }
                                +order.nextStatusLabel!!
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

        BestellingActieModal {
            open = showCheckoutModal
            title = "Bent u zeker dat u deze bestelling wilt afrekenen?"
            description = "Totale prijs: € ${order.totalPrice}"
            confirmLabel = "Bevestigen"
            loading = false
            error = null
            onClose = { setShowCheckoutModal(false) }
            onConfirm = {
                setShowCheckoutModal(false)
                props.onCheckout?.invoke()
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