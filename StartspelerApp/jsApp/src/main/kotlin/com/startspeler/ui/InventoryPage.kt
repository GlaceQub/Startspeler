package com.startspeler.ui

import kotlinx.browser.window
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
    var categoryId: Int?
    var categoryName: String?
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
    var query by useState("")

    Box {
        asDynamic().className = "inventoryRoot"

        Box {
            asDynamic().className = "inventoryLeft"

            Box {
                sx = js("{ pl: 2 }")
                asDynamic().className = "inventoryHeader"
                Typography { asDynamic().variant = "h6"; +"Stock beheer" }
            }

            Box {
                sx = js("{ pl: 2, display: 'flex', gap: 12, alignItems: 'center', mb: 2 }")
                TextField {
                    asDynamic().className = "inventorySearch"
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
                    asDynamic().className = "inventoryPrimaryBtn"
                    asDynamic().onClick = { window.location.hash = "#/product" }
                    +"Product beheer"
                }
            }

            Paper {
                asDynamic().className = "inventoryPaper"

                if (props.loading) {
                    Box { CircularProgress {} }
                } else if (props.error != null) {
                    Alert { asDynamic().severity = "error"; +props.error }
                } else {
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

                    List {
                        if (filtered.isEmpty()) {
                            ListItem { ListItemText { asDynamic().primary = "Geen inventory items gevonden" } }
                        } else {
                            val sorted = filtered.sortedWith(
                                compareBy<InventoryView> { it.categoryName ?: "Geen categorie" }
                                    .thenBy { it.productId }
                                    .thenBy { it.id }
                            )

                            val grouped = sorted.groupBy { it.categoryName ?: "Geen categorie" }.toList()

                            grouped.forEach { (categoryTitle, items) ->
                                ListSubheader {
                                    asDynamic().component = "div"
                                    +categoryTitle
                                }

                                items.forEach { inv ->
                                    val isLowStock = inv.minimumQuantity != null && inv.quantity < inv.minimumQuantity!!

                                    ListItem {
                                        key = inv.id.toString()
                                        asDynamic().className =
                                            if (isLowStock) "inventoryCard inventoryCardLow" else "inventoryCard"
                                        asDynamic().onClick = { _: dynamic -> props.onSelect(inv) }

                                        ListItemText {
                                            asDynamic().primary =
                                                if (inv.productName.isNullOrBlank()) "Product #${inv.productId}" else inv.productName!!
                                            asDynamic().secondary = "Voorraad: ${inv.quantity} · Min: ${inv.minimumQuantity ?: "-"}"
                                        }
                                        ListItemSecondaryAction {
                                            IconButton {
                                                asDynamic().className = "inventoryAction"
                                                asDynamic().onClick = { e: dynamic -> e.stopPropagation(); props.onEdit(inv) }
                                                Edit()
                                            }
                                            IconButton {
                                                asDynamic().className = "inventoryAction"
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
        }

        Box {
            asDynamic().className = "inventoryRight"
            val selected = props.selectedItem

            Paper {
                asDynamic().className = "inventoryDetailsCard"
                sx = js("{ m: 2 }")

                if (selected == null) {
                    div { asDynamic().className = "inventoryDetailsTitle"; +"Selecteer een inventory item" }
                    div { +"Klik links op een item om details te zien of kies 'Nieuw Inventory item'." }
                } else {
                    ListItemText {
                        asDynamic().primary = selected.productName ?: "Product #${selected.productId}"
                        asDynamic().secondary = "Inventory id: ${selected.id}"
                    }
                    div { asDynamic().className = "inventoryDetailsRow"; +"Voorraad: ${selected.quantity}" }
                    div { asDynamic().className = "inventoryDetailsRow"; +"Minimale voorraad: ${selected.minimumQuantity ?: "-"}" }
                    div { asDynamic().className = "inventoryDetailsRow"; +"Laatst bijgewerkt: ${selected.lastUpdated ?: "-"}" }
                    Box {
                        sx = js("{ display: 'flex', gap: 8, marginTop: 12 }")
                        Button { asDynamic().className = "inventoryPrimaryBtn"; asDynamic().onClick = { props.onEdit(selected) }; +"Bewerk" }
                        Button { variant = ButtonVariant.outlined; asDynamic().onClick = { props.onDelete(selected) }; +"Verwijder" }
                    }
                }
            }
        }
    }
}