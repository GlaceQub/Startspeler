package com.startspeler.components.bestellingen

import mui.icons.material.AccessTime
import mui.icons.material.Clear
import mui.icons.material.Search
import mui.material.Box
import mui.material.Button
import mui.material.IconButton
import mui.material.TextField
import mui.material.Typography
import react.FC
import react.Props
import react.ReactNode
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML

external interface BestellingenFilterBarProps : Props {
    var filter: String
    var onFilterChange: (String) -> Unit
    var statusOptions: List<String>
    var selectedStatuses: List<String>
    var onStatusChange: (List<String>) -> Unit
    var selectedDate: String
    var onSelectedDateChange: (String) -> Unit
    var onApplyDateFilter: () -> Unit
    var clientOptions: List<String>
    var selectedClient: String
    var clientInputError: String?
    var onSelectedClientChange: (String) -> Unit
    var onOpenBulkCheckout: () -> Unit
}

val BestellingenFilterBar = FC<BestellingenFilterBarProps> { props ->
    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px' }")

        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }")
            Box {
                sx = js("{ display: 'flex', alignItems: 'center', minWidth: '30vw', maxWidth: '40vw', marginBottom: '8px' }")
                Search {
                    sx = js("{ marginTop: 'auto', marginBottom: 'auto', marginRight: '8px', color: '#888' }")
                }
                TextField {
                    label = ReactNode("Zoek klant of tafel")
                    value = props.filter
                    variant = mui.material.FormControlVariant.standard
                    asDynamic().onChange = { event: ChangeEvent<*> ->
                        props.onFilterChange(event.target.asDynamic().value as String)
                    }
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
                sx = js("{ display: 'flex', gap: '8px', flexWrap: 'wrap' }")
                props.statusOptions.forEach { status ->
                    val selected = props.selectedStatuses.contains(status)
                    Button {
                        variant = if (selected) mui.material.ButtonVariant.contained else mui.material.ButtonVariant.outlined
                        asDynamic().color = if (selected) "primary" else "inherit"
                        onClick = {
                            val newStatuses = if (selected) props.selectedStatuses.filter { it != status } else props.selectedStatuses + status
                            props.onStatusChange(newStatuses)
                        }
                        sx = js("{ textTransform: 'none', fontWeight: 500 }")
                        +status
                    }
                }
            }
        }

        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'flex-end', gap: '12px', flexWrap: 'wrap', marginBottom: '8px', width: '100%' }")
            Box {
                sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'flex-end', gap: '12px', flexWrap: 'wrap' }")
                TextField {
                    label = ReactNode("Datum")
                    asDynamic().type = "date"
                    value = props.selectedDate
                    variant = mui.material.FormControlVariant.outlined
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
                Box {
                    sx = js("{ display: 'flex', alignItems: 'center', gap: '4px', padding: '4px 10px', background: '#e3f2fd', borderRadius: '16px' }")
                    AccessTime {
                        sx = js("{ fontSize: '0.95rem', color: '#1976d2' }")
                    }
                    Typography {
                        sx = js("{ fontSize: '0.8rem', color: '#1976d2', whiteSpace: 'nowrap' }")
                        +"Tot 48u in het verleden"
                    }
                }
            }
            Box {
                sx = js("{ flex: 1 }")
            }
            Box {
                sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'flex-end', gap: '12px', flexWrap: 'wrap', justifyContent: 'flex-end', marginLeft: 'auto' }")
                Box {
                    sx = js("{ display: 'flex', flexDirection: 'column', minWidth: '260px' }")
                    TextField {
                        label = ReactNode("Klant")
                        value = props.selectedClient
                        variant = mui.material.FormControlVariant.outlined
                        size = mui.material.Size.small
                        error = props.clientInputError != null
                        helperText = props.clientInputError?.let { ReactNode(it) }
                        asDynamic().onChange = { event: ChangeEvent<*> ->
                            props.onSelectedClientChange(event.target.asDynamic().value as String)
                        }
                        asDynamic().inputProps = js("{ list: 'bestellingen-klanten' }")
                    }
                    ReactHTML.datalist {
                        asDynamic().id = "bestellingen-klanten"
                        props.clientOptions.forEach { client ->
                            ReactHTML.option {
                                value = client
                            }
                        }
                    }
                }
                Button {
                    variant = mui.material.ButtonVariant.outlined
                    color = mui.material.ButtonColor.primary
                    size = mui.material.Size.small
                    onClick = { props.onOpenBulkCheckout() }
                    +"Openstaande rekeningen"
                }
            }
        }
    }
}
