package com.startspeler.ui

import mui.material.*
import mui.system.Box
import mui.icons.material.Edit
import mui.icons.material.Delete
import react.FC
import react.Props
import react.useState
import react.dom.html.ReactHTML.div

external interface InventoryView {
    var id: Int
    var productId: Int
    var productName: String?
    var quantity: Int
    var minimumQuantity: Int?
    var lastUpdated: String?
}

external interface InventoryPageProps : Props {
    var items: List<InventoryView>
    var loading: Boolean
    var error: String?
    var onAdd: () -> Unit
    var onEdit: (InventoryView) -> Unit
    var onDelete: (InventoryView) -> Unit
    var onSelect: (InventoryView?) -> Unit
    var selectedItem: InventoryView?
}

val InventoryPage = FC<InventoryPageProps> { props ->
    // local search state
    var query by useState("")

    Box {
        sx = js("{ display: 'flex', flexDirection: 'row', width: '100%', minHeight: '70vh', boxSizing: 'border-box' }")

        // Left column: list + controls
        Box {
            sx = js("{ flex: '0 0 65%', paddingRight: '24px', boxSizing: 'border-box', minWidth: 0 }")

            // Top controls
            Box {
                sx = js("{ display: 'flex', gap: 12, alignItems: 'center', mb: 1 }")
                Button {
                    variant = ButtonVariant.outlined
                    asDynamic().onClick = { props.onSelect(null) }
                    +"Terug"
                }
                TextField {
                    asDynamic().placeholder = "Zoek (productnaam of id)"
                    asDynamic().variant = "outlined"
                    asDynamic().size = "small"
                    asDynamic().value = query
                    asDynamic().onChange = { e: dynamic ->
                        val v = (e.target as? org.w3c.dom.HTMLInputElement)?.value ?: ""
                        query = v
                    }
                    sx = js("{ flex: '1 1 auto' }")
                }
                Button {
                    variant = ButtonVariant.contained
                    asDynamic().onClick = { props.onAdd() }
                    +"Nieuw Inventory item"
                }
            }

            // Loading / error handling
            if (props.loading) {
                Box { CircularProgress {} }
            } else if (props.error != null) {
                Alert {
                    asDynamic().severity = "error"
                    +props.error
                }
            } else {
                // Filtered list
                val q = query.trim().lowercase()
                val filtered = props.items.filter { item ->
                    if (q.isEmpty()) true
                    else {
                        val name = item.productName ?: ""
                        name.lowercase().contains(q) ||
                                item.productId.toString().contains(q) ||
                                item.id.toString().contains(q)
                    }
                }

                Paper {
                    asDynamic().sx = js("{ mt: 2 }")
                    List {
                        if (filtered.isEmpty()) {
                            ListItem { ListItemText { asDynamic().primary = "Geen inventory items gevonden" } }
                        } else {
                            filtered.forEach { inv ->
                                ListItem {
                                    key = inv.id.toString()
                                    // set onClick via asDynamic to avoid implicit-receiver typing issue
                                    asDynamic().onClick = { _: dynamic -> props.onSelect(inv) }
                                    ListItemText {
                                        asDynamic().primary = if (inv.productName.isNullOrBlank()) "Product #${inv.productId}" else inv.productName!!
                                        asDynamic().secondary = "Voorraad: ${inv.quantity} · Min: ${inv.minimumQuantity ?: "-"}"
                                    }
                                    ListItemSecondaryAction {
                                        IconButton {
                                            asDynamic().onClick = { e: dynamic -> e.stopPropagation(); props.onEdit(inv) }
                                            Edit()
                                        }
                                        IconButton {
                                            asDynamic().onClick = { e: dynamic -> e.stopPropagation(); props.onDelete(inv) }
                                            Delete()
                                        }
                                    }
                                }
                                Divider {}
                            }
                        }
                    }
                }
            }
        }

        // Right column: details for selected item
        Box {
            sx = js("{ flex: '0 0 35%', paddingLeft: '24px', boxSizing: 'border-box', minWidth: 0 }")
            val selected = props.selectedItem
            if (selected == null) {
                Box { asDynamic().style = js("{ padding: 12 }")
                    div { asDynamic().style = js("{ fontWeight: 700, marginBottom: 8 }"); +"Selecteer een inventory item" }
                    div { +"Klik links op een item om details te zien of kies 'Nieuw Inventory item'." }
                }
            } else {
                Paper {
                    asDynamic().sx = js("{ padding: 12 }")
                    ListItemText {
                        asDynamic().primary = selected.productName ?: "Product #${selected.productId}"
                        asDynamic().secondary = "Inventory id: ${selected.id}"
                    }
                    div { +"Voorraad: ${selected.quantity}" }
                    div { +"Minimale voorraad: ${selected.minimumQuantity ?: "-"}" }
                    div { +"Laatst bijgewerkt: ${selected.lastUpdated ?: "-"}" }
                    Box { asDynamic().style = js("{ display: 'flex', gap: 8, marginTop: 12 }")
                        Button { asDynamic().onClick = { props.onEdit(selected) }; +"Bewerk" }
                        Button { asDynamic().onClick = { props.onDelete(selected) }; +"Verwijder" }
                    }
                }
            }
        }
    }
}