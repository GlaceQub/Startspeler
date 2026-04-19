package com.startspeler.ui

import mui.icons.material.Delete
import mui.icons.material.Edit
import mui.material.Alert
import mui.material.Button
import mui.material.ButtonColor
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
import mui.material.MenuItem
import mui.material.TextField
import mui.material.Typography
import mui.system.Box
import react.FC
import react.Props
import react.dom.onChange
import react.useEffect
import react.useMemo
import react.useState

external interface KlantView {
    var id: Int
    var name: String
    var email: String?
    var groupId: Int
    var roleId: Int
    var statusId: Int
}

external interface KlantenPageProps : Props {
    var items: List<KlantView>
    var loading: Boolean
    var error: String?
    var onSearch: (name: String, email: String) -> Unit
    var onUpdate: (id: Int, name: String, email: String?, groupId: Int?) -> Unit
    var onDelete: (id: Int) -> Unit
    var groupNameById: Map<Int, String>
}

val KlantenPage = FC<KlantenPageProps> { props ->
    val (filterName, setFilterName) = useState("")
    val (filterEmail, setFilterEmail) = useState("")

    val (editOpen, setEditOpen) = useState(false)
    val (editId, setEditId) = useState<Int?>(null)
    val (editName, setEditName) = useState("")
    val (editEmail, setEditEmail) = useState("")
    val (editGroupId, setEditGroupId) = useState<Int?>(null)

    val (deleteOpen, setDeleteOpen) = useState(false)
    val (deleteId, setDeleteId) = useState<Int?>(null)

    val hasNoResults = useMemo(props.items, props.loading, props.error) {
        !props.loading && props.error == null && props.items.isEmpty()
    }

    useEffect(filterName, filterEmail) {
        props.onSearch(filterName.trim(), filterEmail.trim())
    }

    // Build dropdown options (sorted by group name)
    val groupOptions: List<Pair<Int, String>> = props.groupNameById
        .entries
        .map { it.key to it.value }
        .sortedBy { it.second.lowercase() }

    Box {
        sx =
            js("{ width: '100vw', minHeight: 'calc(100vh - 60px)', display: 'flex', justifyContent: 'center', alignItems: 'flex-start', pt: 3, pb: 4, boxSizing: 'border-box' }")

        Box {
            sx = js("{ width: '100%', maxWidth: '1100px', px: 3, boxSizing: 'border-box' }")

            Box {
                sx = js("{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }")
                Typography { asDynamic().variant = "h6"; +"Klanten" }
            }

            Box {
                sx = js("{ display: 'flex', gap: 12, flexWrap: 'wrap', mb: 2 }")

                TextField {
                    label = react.ReactNode("Zoek op naam")
                    value = filterName
                    onChange = { e -> setFilterName(e.target.asDynamic().value as String) }
                }

                TextField {
                    label = react.ReactNode("Zoek op e-mail")
                    value = filterEmail
                    onChange = { e -> setFilterEmail(e.target.asDynamic().value as String) }
                }

                Button {
                    variant = ButtonVariant.outlined
                    onClick = { props.onSearch(filterName.trim(), filterEmail.trim()) }
                    +"Zoeken"
                }
            }

            if (props.loading) {
                Box { sx = js("{ mt: 2 }"); CircularProgress {} }
            } else if (props.error != null) {
                Box { sx = js("{ mt: 2 }"); Alert { asDynamic().severity = "error"; +props.error!! } }
            } else if (hasNoResults) {
                Box {
                    sx = js("{ mt: 2 }")
                    Alert { asDynamic().severity = "info"; +"Geen klant met deze naam/e-mailadres gevonden in het systeem." }
                }
            } else {
                Box {
                    sx = js("{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }")

                    props.items.forEach { u ->
                        Card {
                            key = u.id.toString()

                            CardContent {
                                Typography {
                                    asDynamic().component = "h3"
                                    sx = js("{ margin: 0, fontWeight: 700 }")
                                    +u.name
                                }

                                Typography { sx = js("{ mt: 0.5 }"); +(u.email ?: "—") }
                                val groupName = props.groupNameById[u.groupId] ?: u.groupId.toString()
                                Typography { sx = js("{ mt: 0.5 }"); +"Groep: $groupName" }

                                Box {
                                    sx = js("{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 1 }")

                                    IconButton {
                                        color = IconButtonColor.primary
                                        onClick = {
                                            setEditId(u.id)
                                            setEditName(u.name)
                                            setEditEmail(u.email ?: "")
                                            setEditGroupId(u.groupId)
                                            setEditOpen(true)
                                        }
                                        Edit()
                                    }

                                    IconButton {
                                        color = IconButtonColor.error
                                        onClick = {
                                            setDeleteId(u.id)
                                            setDeleteOpen(true)
                                        }
                                        Delete()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Dialog {
                open = editOpen
                onClose = { _, _ -> setEditOpen(false) }

                DialogTitle { +"Klant aanpassen" }

                DialogContent {
                    TextField {
                        label = react.ReactNode("Naam")
                        value = editName
                        onChange = { e -> setEditName(e.target.asDynamic().value as String) }
                        fullWidth = true
                        margin = FormControlMargin.normal
                    }

                    TextField {
                        label = react.ReactNode("E-mail (optioneel)")
                        value = editEmail
                        onChange = { e -> setEditEmail(e.target.asDynamic().value as String) }
                        fullWidth = true
                        margin = FormControlMargin.normal
                    }

                    TextField {
                        select = true
                        label = react.ReactNode("Groep")
                        value = (editGroupId ?: "").toString()
                        onChange = { e ->
                            val v = e.target.asDynamic().value as String
                            setEditGroupId(v.toIntOrNull())
                        }
                        fullWidth = true
                        margin = FormControlMargin.normal

                        // options
                        groupOptions.forEach { (id, name) ->
                            MenuItem {
                                value = id.toString()
                                +name
                            }
                        }
                    }
                }

                DialogActions {
                    Button { onClick = { setEditOpen(false) }; +"Annuleren" }
                    Button {
                        variant = ButtonVariant.contained
                        onClick = {
                            val id = editId
                            if (id != null) {
                                props.onUpdate(
                                    id,
                                    editName.trim(),
                                    editEmail.trim().ifBlank { null },
                                    editGroupId
                                )
                                setEditOpen(false)
                            }
                        }
                        +"Opslaan"
                    }
                }
            }

            Dialog {
                open = deleteOpen
                onClose = { _, _ -> setDeleteOpen(false) }

                DialogTitle { +"Klant verwijderen?" }

                DialogActions {
                    Button { onClick = { setDeleteOpen(false) }; +"Annuleren" }
                    Button {
                        color = ButtonColor.error
                        variant = ButtonVariant.contained
                        onClick = {
                            val id = deleteId
                            if (id != null) {
                                props.onDelete(id)
                                setDeleteOpen(false)
                            }
                        }
                        +"Verwijderen"
                    }
                }
            }
        }
    }
}