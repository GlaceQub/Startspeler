package com.startspeler.ui

import com.startspeler.components.bestellingen.OrderOverzichtItem
import com.startspeler.dto.OrderOverzichtItem
import kotlinx.browser.window
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
    var onCheckoutSuccess: (() -> Unit)?
    var selectedDate: String
    var onSelectedDateChange: (String) -> Unit
    var onApplyDateFilter: () -> Unit
}

val BackendUrl = js("window.BackendUrl") as? String ?: "http://localhost:8080"

val BestellingenPage = FC<BestellingenPageProps> { props ->

    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '16px', width: '100%', boxSizing: 'border-box' }")

        // Filter rij: zoek + status knoppen
        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap' }")
            Box {
                sx = js("{ display: 'flex', alignItems: 'center', minWidth: '30vw', maxWidth: '40vw', marginBottom: '8px' }")
                Search {
                    sx = js("{marginTop: 'auto', marginBottom: 'auto', marginRight: '8px', color: '#888' }")
                }
                TextField {
                    label = react.ReactNode("Zoek klant of tafel")
                    value = props.filter
                    variant = FormControlVariant.standard
                    asDynamic().onChange =
                        { event: ChangeEvent<*> -> props.onFilterChange(event.target.asDynamic().value as String) }
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
            Box {
                sx = js("{ display: 'flex', gap: '8px', marginLeft: '16px'}")
                props.statusOptions.forEach { status ->
                    val selected = props.selectedStatuses.contains(status)
                    Button {
                        variant =
                            if (selected) mui.material.ButtonVariant.contained else mui.material.ButtonVariant.outlined
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
        }

        // Datum filter rij
        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: '12px', flexWrap: 'wrap', marginBottom: '4px' }")
            TextField {
                label = react.ReactNode("Datum")
                asDynamic().type = "date"
                value = props.selectedDate
                variant = FormControlVariant.outlined
                size = mui.material.Size.small
                asDynamic().onChange = { event: ChangeEvent<*> ->
                    props.onSelectedDateChange(event.target.asDynamic().value as String)
                }
                asDynamic().InputLabelProps = js("{ shrink: true }")
                sx = js("{ minWidth: '180px' }")
            }
            Button {
                variant = mui.material.ButtonVariant.contained
                color = mui.material.ButtonColor.primary
                size = mui.material.Size.small
                onClick = { props.onApplyDateFilter() }
                +"Toepassen"
            }
            if (props.selectedDate.isNotEmpty()) {
                Box {
                    sx = js("{ display: 'flex', alignItems: 'center', gap: '4px', padding: '4px 10px', background: '#e3f2fd', borderRadius: '16px' }")
                    mui.icons.material.AccessTime {
                        sx = js("{ fontSize: '0.95rem', color: '#1976d2' }")
                    }
                    Typography {
                        sx = js("{ fontSize: '0.8rem', color: '#1976d2', whiteSpace: 'nowrap' }")
                        +"Toont 48u in het verleden"
                    }
                }
            }
        }

        // Header row boven de bestellingen
        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: '4px 16px', borderBottom: '1px solid #e0e0e0', background: '#fafafa', fontSize: '0.95rem', fontWeight: 600, color: '#666', gap: '16px' }")
            Typography {
                sx = js("{ width: '80px', minWidth: '80px', maxWidth: '80px', paddingLeft: '8px' }")
                +"ID"
            }
            Typography {
                sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px' }")
                +"Tafel"
            }
            Typography {
                sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px' }")
                +"Klant"
            }
            Typography {
                sx = js("{ width: '120px', minWidth: '120px', maxWidth: '120px' }")
                +"Status"
            }
            Typography {
                sx = js("{ width: '180px', minWidth: '180px', maxWidth: '180px' }")
                +"Aangemaakt op"
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
            // Bij het renderen van OrderOverzichtItem
            filteredOrders.forEach { order ->
                OrderOverzichtItem {
                    this.order = order
                    this.isOpen = false
                    this.onCheckout = {
                        val url = "$BackendUrl/order/${order.id}/checkout"
                        window.fetch(url, js("{ method: 'POST' }"))
                            .then({ resp: dynamic ->
                                if ((resp.ok as Boolean)) {
                                    props.onCheckoutSuccess?.invoke()
                                }
                            }, { _: dynamic -> })
                    }
                    this.onSetInBehandeling = {
                        val url = "$BackendUrl/order/${order.id}/inbehandeling"
                        window.fetch(url, js("{ method: 'POST' }"))
                            .then({ resp: dynamic ->
                                if ((resp.ok as Boolean)) {
                                    props.onCheckoutSuccess?.invoke()
                                }
                            }, { _: dynamic -> })
                    }
                }
            }
        }
    }
}