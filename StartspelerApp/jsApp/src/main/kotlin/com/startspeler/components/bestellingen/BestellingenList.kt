package com.startspeler.components.bestellingen

import com.startspeler.dto.OrderOverzichtItem
import mui.material.Box
import mui.material.CircularProgress
import mui.material.Typography
import react.FC
import react.Props

external interface BestellingenListProps : Props {
    var orders: List<OrderOverzichtItem>
    var loading: Boolean
    var error: String?
    var filter: String
    var selectedStatuses: List<String>
    var onCheckout: (String) -> Unit
    var onDelete: (Int) -> Unit
    var canDeleteOrders: Boolean
    var onMoveToNextStatus: (Int) -> Unit
    var onMoveToPreviousStatus: (Int) -> Unit
}

val BestellingenList = FC<BestellingenListProps> { props ->
    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '0' }")

        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: '4px 16px', borderBottom: '1px solid #e0e0e0', background: '#fafafa', fontSize: '0.95rem', fontWeight: 600, color: '#666', gap: '16px' }")
            Typography { sx = js("{ width: '80px', minWidth: '80px', maxWidth: '80px', paddingLeft: '8px' }"); +"ID" }
            Typography { sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px' }"); +"Tafel" }
            Typography { sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px' }"); +"Klant" }
            Typography { sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px' }"); +"Status" }
            Typography { sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px' }"); +"Aangemaakt op" }
        }

        if (props.loading) {
            CircularProgress {}
        } else if (props.error != null) {
            Typography { +props.error!! }
        } else {
            val filteredOrders = props.orders.filter {
                val f = props.filter.trim().lowercase()
                val statusActive = props.selectedStatuses.contains(it.status)
                statusActive && (
                    f.isEmpty() ||
                        it.clientName.lowercase().contains(f) ||
                        it.tableNumber.toString().contains(f)
                    )
            }

            filteredOrders.forEach { order ->
                OrderOverzichtItem {
                    this.order = order
                    this.isOpen = false
                    this.onCheckout = { props.onCheckout(order.clientName) }
                    this.onDelete = { props.onDelete(order.id) }
                    this.canDelete = props.canDeleteOrders
                    this.onMoveToNextStatus = { props.onMoveToNextStatus(order.id) }
                    this.onMoveToPreviousStatus = { props.onMoveToPreviousStatus(order.id) }
                }
            }
        }
    }
}
