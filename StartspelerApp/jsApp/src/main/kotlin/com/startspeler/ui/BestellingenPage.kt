package com.startspeler.ui

import com.startspeler.components.bestellingen.BestellingenFilterBar
import com.startspeler.components.bestellingen.BestellingenList
import com.startspeler.components.bestellingen.BulkCheckoutModal
import com.startspeler.dto.ClientOpenOrdersSummary
import com.startspeler.dto.OrderOverzichtItem
import mui.material.Box
import react.FC
import react.Props

external interface BestellingenPageProps : Props {
    var orders: List<OrderOverzichtItem>
    var loading: Boolean
    var error: String?
    var filter: String
    var onFilterChange: (String) -> Unit
    var statusOptions: List<String>
    var selectedStatuses: List<String>
    var onStatusChange: (List<String>) -> Unit
    var selectedDate: String
    var onSelectedDateChange: (String) -> Unit
    var onApplyDateFilter: () -> Unit
    var onCheckout: (Int) -> Unit
    var onDelete: (Int) -> Unit
    var canDeleteOrders: Boolean
    var onMoveToNextStatus: (Int) -> Unit
    var onMoveToPreviousStatus: (Int) -> Unit
    var clientOptions: List<String>
    var selectedClient: String
    var clientInputError: String?
    var onSelectedClientChange: (String) -> Unit
    var onOpenBulkCheckout: () -> Unit
    var bulkSummary: ClientOpenOrdersSummary?
    var bulkModalOpen: Boolean
    var onBulkModalClose: () -> Unit
    var onConfirmBulkCheckout: () -> Unit
    var bulkLoading: Boolean
    var bulkError: String?
}

val BestellingenPage = FC<BestellingenPageProps> { props ->
    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '16px', width: '100%', boxSizing: 'border-box' }")

        BestellingenFilterBar {
            filter = props.filter
            onFilterChange = props.onFilterChange
            statusOptions = props.statusOptions
            selectedStatuses = props.selectedStatuses
            onStatusChange = props.onStatusChange
            selectedDate = props.selectedDate
            onSelectedDateChange = props.onSelectedDateChange
            onApplyDateFilter = props.onApplyDateFilter
            clientOptions = props.clientOptions
            selectedClient = props.selectedClient
            clientInputError = props.clientInputError
            onSelectedClientChange = props.onSelectedClientChange
            onOpenBulkCheckout = props.onOpenBulkCheckout
        }

        BestellingenList {
            orders = props.orders
            loading = props.loading
            error = props.error
            filter = props.filter
            selectedStatuses = props.selectedStatuses
            onCheckout = props.onCheckout
            onDelete = props.onDelete
            canDeleteOrders = props.canDeleteOrders
            onMoveToNextStatus = props.onMoveToNextStatus
            onMoveToPreviousStatus = props.onMoveToPreviousStatus
        }

        BulkCheckoutModal {
            open = props.bulkModalOpen
            summary = props.bulkSummary
            loading = props.bulkLoading
            error = props.bulkError
            onClose = props.onBulkModalClose
            onConfirm = props.onConfirmBulkCheckout
        }
    }
}