package com.startspeler.ui

import com.startspeler.dto.CategoryDto
import kotlinx.browser.window
import mui.material.*
import mui.material.Size
import mui.system.Box
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.useState

external interface ProductView {
    var id: Int
    var name: String
    var categoryId: Int
    var categoryName: String
    var price: Float
    var popularity: Int
}

external interface ProductPageProps : Props {
    var items: List<ProductView>
    var categories: List<CategoryDto>
    var loading: Boolean
    var error: String?
    var onAdd: (name: String, categoryId: Int, price: Float, popularity: Int) -> Unit
    var onEdit: (id: Int, name: String, categoryId: Int, price: Float, popularity: Int) -> Unit
    var onDelete: (id: Int) -> Unit
}

val ProductPage = FC<ProductPageProps> { props ->
    var dialogOpen by useState(false)
    var editingId by useState<Int?>(null)

    var name by useState("")
    var categoryIdStr by useState("")
    var priceStr by useState("")
    var popularityStr by useState("0")

    // sort/filter
    var sortKey by useState("name") // "name" | "price" | "popularity"
    var sortDir by useState("asc")  // "asc" | "desc"
    var selectedCategory by useState<String?>(null)

    // category dropdown state
    var categoryDropdownOpen by useState(false)

    val toggleSort: (String) -> Unit = { key ->
        if (sortKey == key) sortDir = if (sortDir == "asc") "desc" else "asc"
        else {
            sortKey = key
            sortDir = "asc"
        }
    }

    val sortIndicator: (String) -> String = { key ->
        if (sortKey != key) "" else if (sortDir == "asc") " ▲" else " ▼"
    }

    val categoriesDistinct = props.items.map { it.categoryName }.distinct().sorted()

    val visibleItems = props.items
        .asSequence()
        .filter { selectedCategory == null || it.categoryName == selectedCategory }
        .sortedWith { a, b ->
            val cmp = when (sortKey) {
                "name" -> a.name.lowercase().compareTo(b.name.lowercase())
                "price" -> a.price.compareTo(b.price)
                "popularity" -> a.popularity.compareTo(b.popularity)
                else -> 0
            }
            if (sortDir == "asc") cmp else -cmp
        }
        .toList()

    val openNew = {
        editingId = null
        name = ""
        categoryIdStr = ""
        priceStr = ""
        popularityStr = "0"
        dialogOpen = true
    }

    val openEdit: (ProductView) -> Unit = { p ->
        editingId = p.id
        name = p.name
        categoryIdStr = p.categoryId.toString()
        priceStr = p.price.toString()
        popularityStr = p.popularity.toString()
        dialogOpen = true
    }

    Box {
        sx = js("{ width: '100vw', minHeight: 'calc(100vh - 60px)', display: 'flex', justifyContent: 'center', alignItems: 'flex-start', pt: 3, pb: 4, boxSizing: 'border-box' }")

        Box {
            sx = js("{ width: '100%', maxWidth: 'none', px: 3, boxSizing: 'border-box' }")

            Box {
                sx = js("{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }")
                Typography { asDynamic().variant = "h6"; +"Product beheer" }
                Button {
                    sx = js("{ mt: 2, backgroundColor: 'var(--startspeler-primary)', color: 'white', fontWeight: 700, borderRadius: '20px', boxShadow: 'none', '&:hover': { backgroundColor: '#22356a', boxShadow: 'none' } }")
                    variant = ButtonVariant.contained
                    asDynamic().onClick = { openNew() }
                    +"Nieuw product"
                }
            }

            if (props.loading) {
                Box { CircularProgress {} }
            } else if (props.error != null) {
                Alert { asDynamic().severity = "error"; +props.error!! }
            } else {
                TableContainer {
                    component = Paper
                    sx = js("{ width: '100%' }")

                    Table {
                        size = Size.medium
                        sx = js("{ tableLayout: 'fixed', width: '100%', '& th, & td': { fontSize: '1rem', py: 1.5 } }")

                        TableHead {
                            TableRow {
                                TableCell { +"ID" }

                                TableCell {
                                    ButtonBase {
                                        sx = js("{ width: '100%', justifyContent: 'flex-start', fontWeight: 800 }")
                                        asDynamic().onClick = { toggleSort("name") }
                                        Typography { +"Naam${sortIndicator("name")}" }
                                    }
                                }

                                // Category header: absolute dropdown over the table (no header height changes)
                                TableCell {
                                    Box {
                                        sx = js("{ position: 'relative', display: 'inline-flex', alignItems: 'center', gap: '6px' }")

                                        Typography { +"Categorie" }

                                        IconButton {
                                            size = Size.small
                                            sx = js("{ p: '4px' }")
                                            asDynamic().onClick = { categoryDropdownOpen = !categoryDropdownOpen }
                                            +"▾"
                                        }

                                        if (categoryDropdownOpen) {
                                            Paper {
                                                elevation = 6
                                                sx = js("{ position: 'absolute', top: 'calc(100% + 8px)', left: 0, zIndex: 2000, minWidth: '180px', borderRadius: '12px', overflow: 'hidden' }")

                                                MenuList {
                                                    MenuItem {
                                                        key = "all"
                                                        asDynamic().selected = selectedCategory == null
                                                        asDynamic().onClick = {
                                                            selectedCategory = null
                                                            categoryDropdownOpen = false
                                                        }
                                                        +"Alle"
                                                    }

                                                    categoriesDistinct.forEach { cat ->
                                                        MenuItem {
                                                            key = cat
                                                            asDynamic().selected = selectedCategory == cat
                                                            asDynamic().onClick = {
                                                                selectedCategory = cat
                                                                categoryDropdownOpen = false
                                                            }
                                                            +cat
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                TableCell {
                                    ButtonBase {
                                        sx = js("{ width: '100%', justifyContent: 'flex-start', fontWeight: 800 }")
                                        asDynamic().onClick = { toggleSort("price") }
                                        Typography { +"Prijs${sortIndicator("price")}" }
                                    }
                                }

                                TableCell {
                                    ButtonBase {
                                        sx = js("{ width: '100%', justifyContent: 'flex-start', fontWeight: 800 }")
                                        asDynamic().onClick = { toggleSort("popularity") }
                                        Typography { +"Populariteit${sortIndicator("popularity")}" }
                                    }
                                }

                                TableCell { +"Acties" }
                            }
                        }

                        TableBody {
                            visibleItems.forEach { p ->
                                TableRow {
                                    key = p.id.toString()
                                    TableCell { +p.id.toString() }
                                    TableCell { +p.name }
                                    TableCell { +p.categoryName }
                                    TableCell { +p.price.toString() }
                                    TableCell { +p.popularity.toString() }
                                    TableCell {
                                        Box {
                                            sx = js("{ display: 'flex', gap: '12px', alignItems: 'center', mt: 2 }")

                                            Button {
                                                variant = ButtonVariant.outlined
                                                size = Size.small
                                                sx = js("{ textTransform: 'none', fontWeight: 700, borderRadius: '20px' }")
                                                asDynamic().onClick = { openEdit(p) }
                                                +"Wijzigen"
                                            }

                                            Button {
                                                variant = ButtonVariant.outlined
                                                color = ButtonColor.error
                                                size = Size.small
                                                sx = js("{ textTransform: 'none', fontWeight: 700, borderRadius: '20px' }")
                                                asDynamic().onClick = { props.onDelete(p.id) }
                                                +"Verwijder"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Dialog {
                open = dialogOpen
                onClose = { _, _ -> dialogOpen = false }

                DialogTitle {
                    if (editingId == null) +"Nieuw product toevoegen" else +"Product wijzigen (#${editingId})"
                }

                DialogContent {
                    Box {
                        sx = js("({ display: 'flex', flexDirection: 'column', gap: 2, mt: 1, minWidth: 360 })")

                        TextField {
                            asDynamic().label = "Naam"
                            asDynamic().value = name
                            asDynamic().onChange = { e: dynamic -> name = (e.target as HTMLInputElement).value }
                            asDynamic().fullWidth = true
                        }

                        TextField {
                            asDynamic().label = "Categorie"
                            asDynamic().select = true
                            asDynamic().value = categoryIdStr
                            asDynamic().onChange = { e: dynamic ->
                                categoryIdStr = (e.target.value as? String) ?: e.target.value.toString()
                            }
                            asDynamic().fullWidth = true

                            props.categories.forEach { c ->
                                MenuItem {
                                    key = c.id.toString()
                                    asDynamic().value = c.id.toString()
                                    +c.name
                                }
                            }
                        }

                        TextField {
                            asDynamic().label = "Prijs"
                            asDynamic().value = priceStr
                            asDynamic().onChange = { e: dynamic -> priceStr = (e.target as HTMLInputElement).value }
                            asDynamic().fullWidth = true
                        }

                        TextField {
                            asDynamic().label = "Populariteit"
                            asDynamic().value = popularityStr
                            asDynamic().onChange = { e: dynamic -> popularityStr = (e.target as HTMLInputElement).value }
                            asDynamic().fullWidth = true
                        }
                    }
                }

                DialogActions {
                    Box {
                        sx = js("{ display: 'flex', gap: '12px', alignItems: 'center' }")

                        Button {
                            variant = ButtonVariant.text
                            sx = js("{ textTransform: 'none', fontWeight: 700, borderRadius: '20px' }")
                            asDynamic().onClick = { dialogOpen = false }
                            +"Annuleer"
                        }

                        Button {
                            variant = ButtonVariant.contained
                            sx = js("{ textTransform: 'none', fontWeight: 700, borderRadius: '20px', backgroundColor: 'var(--startspeler-primary)', color: 'white', boxShadow: 'none', '&:hover': { backgroundColor: '#22356a', boxShadow: 'none' } }")
                            asDynamic().onClick = {
                                val catId = categoryIdStr.toIntOrNull()
                                val price = priceStr.toFloatOrNull()
                                val pop = popularityStr.toIntOrNull() ?: 0

                                if (name.isBlank() || catId == null || price == null) {
                                    window.alert("Vul Naam, Categorie en Prijs correct in.")
                                } else {
                                    val id = editingId
                                    if (id == null) props.onAdd(name.trim(), catId, price, pop)
                                    else props.onEdit(id, name.trim(), catId, price, pop)
                                    dialogOpen = false
                                }
                            }
                            +"Opslaan"
                        }
                    }
                }
            }
        }
    }
}