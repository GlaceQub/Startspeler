package com.startspeler.ui

import mui.icons.material.Delete
import mui.material.Alert
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Card
import mui.material.CardContent
import mui.material.CircularProgress
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.FormControlMargin
import mui.material.IconButton
import mui.material.IconButtonColor
import mui.material.Switch
import mui.material.TextField
import mui.system.Box
import react.FC
import react.Props
import react.dom.onChange
import react.useState

external interface TafelView {
    var id: Int
    var number: Int
    var active: Boolean
}

external interface TafelPageProps : Props {
    var items: List<TafelView>
    var loading: Boolean
    var error: String?
    var onToggleActive: (id: Int, newActive: Boolean) -> Unit
    var onAdd: (number: Int) -> Unit
    var onDelete: (id: Int) -> Unit
}

val TafelPage = FC<TafelPageProps> { props ->
    val (open, setOpen) = useState(false)
    val (newNumber, setNewNumber) = useState("")

    Box {
        asDynamic().className = "tafelRoot"
        sx =
            js("{ width: '100vw', minHeight: 'calc(100vh - 60px)', display: 'flex', justifyContent: 'center', alignItems: 'flex-start', pt: 3, pb: 4, boxSizing: 'border-box' }")

        Box {
            sx = js("{ width: '100%', maxWidth: '1100px', px: 3, boxSizing: 'border-box' }")

            Box {
                sx = js("{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }")

                PageTitleBar {
                    title = "Tafels"
                    rightContent = null
                }

                Button {
                    variant = ButtonVariant.contained
                    onClick = { setOpen(true) }
                    +"Tafel toevoegen"
                }
            }

            Dialog {
                this.open = open
                onClose = { _, _ -> setOpen(false) }

                DialogTitle { +"Nieuwe tafel" }

                DialogContent {
                    TextField {
                        label = react.ReactNode("Tafelnummer")
                        value = newNumber
                        onChange = { e -> setNewNumber(e.target.asDynamic().value as String) }
                        asDynamic().type = "number"
                        fullWidth = true
                        margin = FormControlMargin.normal
                    }
                }

                DialogActions {
                    Button {
                        onClick = { setOpen(false) }
                        +"Annuleren"
                    }
                    Button {
                        variant = ButtonVariant.contained
                        onClick = {
                            val n = newNumber.toIntOrNull()
                            if (n != null) {
                                props.onAdd(n)
                                setNewNumber("")
                                setOpen(false)
                            }
                        }
                        +"Opslaan"
                    }
                }
            }

            if (props.loading) {
                Box { sx = js("{ mt: 2 }"); CircularProgress {} }
            } else if (props.error != null) {
                Box { sx = js("{ mt: 2 }"); Alert { asDynamic().severity = "error"; +props.error!! } }
            } else {
                Box {
                    asDynamic().className = "tafelGrid"

                    props.items
                        .sortedBy { it.number }
                        .forEach { t ->
                            Card {
                                key = t.id.toString()
                                asDynamic().className =
                                    "tafelCard " + if (t.active) "tafelCard--active" else "tafelCard--inactive"

                                CardContent {
                                    mui.material.Typography {
                                        asDynamic().component = "h3"
                                        asDynamic().className = "tafelCardTitle"
                                        +"Tafel ${t.number}"
                                    }

                                    Box {
                                        sx = js("{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 1 }")

                                        mui.material.Typography {
                                            asDynamic().component = "div"
                                            asDynamic().className = "tafelCardStatus"
                                            +if (t.active) "Actief" else "Inactief"
                                        }

                                        Switch {
                                            checked = t.active
                                            onChange = { _, checked -> props.onToggleActive(t.id, checked) }
                                        }
                                    }

                                    Box {
                                        asDynamic().className = "tafelCardActions"
                                        sx = js("{ display: 'flex', justifyContent: 'flex-end', mt: 1 }")

                                        IconButton {
                                            color = IconButtonColor.error
                                            onClick = { props.onDelete(t.id) }
                                            Delete()
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }
    }
}