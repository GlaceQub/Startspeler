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

    val groupOptions: List<Pair<Int, String>> = props.groupNameById
        .entries
        .map { it.key to it.value }
        .sortedBy { it.second.lowercase() }

    Box {
        sx = js(
            """({
              width: '100vw',
              minHeight: 'calc(100vh - 60px)',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'flex-start',
              pt: 3,
              pb: 4,
              boxSizing: 'border-box'
            })"""
        )

        Box {
            sx = js(
                """({
                  width: '100%',
                  maxWidth: '1100px',
                  px: 3,
                  boxSizing: 'border-box'
                })"""
            )

            Box {
                sx = js(
                    """({
                      border: '1px solid rgba(0,0,0,0.10)',
                      borderRadius: '12px',
                      backgroundColor: 'rgba(245,247,250,0.7)',
                      px: 2,
                      py: 2,
                      mb: 2
                    })"""
                )

                Box {
                    sx = js("{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }")
                    Typography {
                        asDynamic().variant = "h6"
                        sx = js("{ fontWeight: 700, color: '#2B3078' }")
                        +"Klanten"
                    }
                }

                Box {
                    sx = js(
                        """({
                          display: 'flex',
                          justifyContent: 'flex-start',
                          gap: 12,
                          flexWrap: 'wrap',
                          alignItems: 'center'
                        })"""
                    )

                    Box {
                        sx = js("{ display: 'flex', flexDirection: 'column', gap: 0 }")

                        Typography {
                            sx = js("{ fontSize: '0.8rem', fontWeight: 600, lineHeight: 1, color: 'rgba(0,0,0,0.65)', ml: 0.5, mt: 0, mb: 0, p: 0, display: 'block' }")
                            +"Naam"
                        }

                        TextField {
                            label = react.ReactNode("Zoek op naam")
                            value = filterName
                            asDynamic().size = "small"
                            onChange = { e -> setFilterName(e.target.asDynamic().value as String) }
                        }
                    }

                    Box {
                        sx = js("{ display: 'flex', flexDirection: 'column', gap: 0 }")

                        Typography {
                            sx = js("{ fontSize: '0.8rem', fontWeight: 600, lineHeight: 1, color: 'rgba(0,0,0,0.65)', ml: 0.5, mt: 0, mb: 0, p: 0, display: 'block' }")
                            +"E-mail"
                        }

                        TextField {
                            label = react.ReactNode("Zoek op e-mail")
                            value = filterEmail
                            asDynamic().size = "small"
                            onChange = { e -> setFilterEmail(e.target.asDynamic().value as String) }
                        }
                    }

                    Button {
                        variant = ButtonVariant.outlined
                        asDynamic().size = "small"
                        onClick = { props.onSearch(filterName.trim(), filterEmail.trim()) }
                        +"Zoeken"
                    }
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
                            sx = js("{ borderRadius: '12px', border: '1px solid rgba(0,0,0,0.08)' }")

                            CardContent {
                                sx = js("{ paddingBottom: '12px !important' }")

                                Typography {
                                    asDynamic().component = "h3"
                                    sx = js("{ margin: 0, fontWeight: 700, fontSize: '1.0rem' }")
                                    +u.name
                                }

                                Typography { sx = js("{ mt: 0.5, color: 'rgba(0,0,0,0.7)' }"); +(u.email ?: "—") }
                                val groupName = props.groupNameById[u.groupId] ?: u.groupId.toString()
                                Typography { sx = js("{ mt: 0.5, color: 'rgba(0,0,0,0.75)' }"); +"Groep: $groupName" }

                                Box {
                                    sx = js("{ display: 'flex', justifyContent: 'flex-end', gap: 0.5, mt: 1 }")

                                    IconButton {
                                        color = IconButtonColor.primary
                                        asDynamic().size = "small"
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
                                        asDynamic().size = "small"
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
                        asDynamic().size = "small"
                        onChange = { e -> setEditName(e.target.asDynamic().value as String) }
                        fullWidth = true
                        margin = FormControlMargin.normal
                    }

                    TextField {
                        label = react.ReactNode("E-mail (optioneel)")
                        value = editEmail
                        asDynamic().size = "small"
                        onChange = { e -> setEditEmail(e.target.asDynamic().value as String) }
                        fullWidth = true
                        margin = FormControlMargin.normal
                    }

                    TextField {
                        select = true
                        label = react.ReactNode("Groep")
                        value = (editGroupId ?: "").toString()
                        asDynamic().size = "small"
                        onChange = { e ->
                            val v = e.target.asDynamic().value as String
                            setEditGroupId(v.toIntOrNull())
                        }
                        fullWidth = true
                        margin = FormControlMargin.normal

                        groupOptions.forEach { (id, name) ->
                            MenuItem {
                                value = id.toString()
                                +name
                            }
                        }
                    }
                }

                DialogActions {
                    Button { asDynamic().size = "small"; onClick = { setEditOpen(false) }; +"Annuleren" }
                    Button {
                        asDynamic().size = "small"
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
                    Button { asDynamic().size = "small"; onClick = { setDeleteOpen(false) }; +"Annuleren" }
                    Button {
                        asDynamic().size = "small"
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