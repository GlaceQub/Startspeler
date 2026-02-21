package com.startspeler.ui

import com.startspeler.components.bestellingen.OrderOverzichtItem
import com.startspeler.dto.OrderOverzichtItem
import mui.material.Box
import mui.material.CircularProgress
import mui.material.TextField
import mui.material.Typography
import mui.material.Button
import mui.icons.material.Search
import mui.material.IconButton
import mui.icons.material.Clear
import mui.material.FormControlVariant
import react.FC
import react.Props
import react.dom.events.ChangeEvent

external interface BestellingenPageProps : Props {
    var orders: List<OrderOverzichtItem>
    var loading: Boolean
    var error: String?
    var filter: String
    var onFilterChange: (String) -> Unit
    var statusOptions: List<String>
    var selectedStatuses: List<String>
    var onStatusChange: (List<String>) -> Unit
}

val BestellingenPage = FC<BestellingenPageProps> { props ->
    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '16px', width: '100%', boxSizing: 'border-box' }")
        Box {
            sx = js("{ display: 'flex', alignItems: 'center', marginBottom: '16px', maxWidth: '400px'}")
            Search {
                sx = js("{ marginLeft: '12px', marginRight: '8px', color: '#888' }")
            }
            TextField {
                label = react.ReactNode("Zoek klant of tafel")
                value = props.filter
                variant = FormControlVariant.standard
                asDynamic().onChange = { event: ChangeEvent<*> -> props.onFilterChange(event.target.asDynamic().value as String) }
                sx = js("{ flex: 1, background: 'transparent', border: 'none' }")
                size = mui.material.Size.small
            }
            if (props.filter.isNotEmpty()) {
                IconButton {
                    size = mui.material.Size.small
                    onClick = { props.onFilterChange("") }
                    sx = js("{ marginLeft: '4px', color: '#888' }")
                    Clear {}
                }
            }
        }

        Typography {
            sx = js("{ marginBottom: '4px', fontWeight: 500 }")
            +"Status filter:"
        }
        Box {
            sx = js("{ display: 'flex', gap: '8px', marginBottom: '16px' }")
            props.statusOptions.forEach { status ->
                val selected = props.selectedStatuses.contains(status)
                Button {
                    variant = if (selected) mui.material.ButtonVariant.contained else mui.material.ButtonVariant.outlined
                    asDynamic().color = if (selected) "primary" else "inherit"
                    onClick = {
                        val newStatuses = if (selected) {
                            props.selectedStatuses.filter { it != status }
                        } else {
                            props.selectedStatuses + status
                        }
                        props.onStatusChange(newStatuses)
                    }
                    sx = js("{ textTransform: 'upper', fontWeight: 500 }")
                    +status
                }
            }
        }

        if (props.loading) {
            CircularProgress {}
        } else if (props.error != null) {
            Typography { +"Er is iets fout gegaan bij het ophalen van de bestellingen!" }
        } else {
            val filteredOrders = props.orders.filter {
                val f = props.filter.trim().lowercase()
                val statusActive = props.selectedStatuses.contains(it.status)
                statusActive && (f.isEmpty() ||
                    it.clientName.lowercase().contains(f) ||
                    it.tableNumber.toString().contains(f))
            }
            filteredOrders.forEach { order ->
                OrderOverzichtItem {
                    this.order = order
                    this.isOpen = false
                }
            }
        }
    }
}