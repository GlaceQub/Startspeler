package com.startspeler.ui

import com.startspeler.dto.CategoryDto
import kotlinx.browser.window
import mui.material.*
import mui.material.Size
import mui.system.Box
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.useEffect
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

    // open "new"
    val openNew = {
        editingId = null
        name = ""
        categoryIdStr = ""
        priceStr = ""
        popularityStr = "0"
        dialogOpen = true
    }

    // open "edit"
    val openEdit: (ProductView) -> Unit = { p ->
        editingId = p.id
        name = p.name
        categoryIdStr = p.categoryId.toString()
        priceStr = p.price.toString()
        popularityStr = p.popularity.toString()
        dialogOpen = true
    }

    Box {
        sx = js("{ p: 2 }")

        Box {
            sx = js("{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }")
            Typography { asDynamic().variant = "h6"; +"Product beheer" }
            Button {
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
                Table {
                    TableHead {
                        TableRow {
                            TableCell { +"ID" }
                            TableCell { +"Naam" }
                            TableCell { +"Categorie" }
                            TableCell { +"Prijs" }
                            TableCell { +"Populariteit" }
                            TableCell { +"Acties" }
                        }
                    }
                    TableBody {
                        props.items.forEach { p ->
                            TableRow {
                                key = p.id.toString()
                                TableCell { +p.id.toString() }
                                TableCell { +p.name }
                                TableCell { +p.categoryName }
                                TableCell { +p.price.toString() }
                                TableCell { +p.popularity.toString() }
                                TableCell {
                                    Button {
                                        variant = ButtonVariant.outlined
                                        size = Size.small
                                        asDynamic().onClick = { openEdit(p) }
                                        +"Wijzigen"
                                    }
                                    Button {
                                        variant = ButtonVariant.outlined
                                        color = ButtonColor.error
                                        size = Size.small
                                        sx = js("{ ml: 1 }")
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

        Dialog {
            open = dialogOpen
            onClose = { _, _ -> dialogOpen = false }

            DialogTitle {
                if (editingId == null) +"Nieuw product toevoegen" else +"Product wijzigen (#${editingId})"
            }

            DialogContent {
                Box { sx = js("{ display: 'flex', flexDirection: 'column', gap: 12, mt: 1, minWidth: 360 }") }

                TextField {
                    asDynamic().label = "Naam"
                    asDynamic().value = name
                    asDynamic().onChange = { e: dynamic ->
                        name = (e.target as HTMLInputElement).value
                    }
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
                    asDynamic().onChange = { e: dynamic ->
                        priceStr = (e.target as HTMLInputElement).value
                    }
                    asDynamic().fullWidth = true
                }

                TextField {
                    asDynamic().label = "Populariteit"
                    asDynamic().value = popularityStr
                    asDynamic().onChange = { e: dynamic ->
                        popularityStr = (e.target as HTMLInputElement).value
                    }
                    asDynamic().fullWidth = true
                }
            }

            DialogActions {
                Button {
                    variant = ButtonVariant.text
                    asDynamic().onClick = { dialogOpen = false }
                    +"Annuleer"
                }
                Button {
                    variant = ButtonVariant.contained
                    asDynamic().onClick = {
                        val catId = categoryIdStr.toIntOrNull()
                        val price = priceStr.toFloatOrNull()
                        val pop = popularityStr.toIntOrNull() ?: 0

                        if (name.isBlank() || catId == null || price == null) {
                            window.alert("Vul Naam, Categorie en Prijs correct in.")
                        } else {
                            val id = editingId
                            if (id == null) {
                                props.onAdd(name.trim(), catId, price, pop)
                            } else {
                                props.onEdit(id, name.trim(), catId, price, pop)
                            }
                            dialogOpen = false
                        }
                    }
                    +"Opslaan"
                }
            }
        }
    }
}