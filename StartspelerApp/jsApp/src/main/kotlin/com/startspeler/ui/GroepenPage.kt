package com.startspeler.ui

import com.startspeler.components.groepen.GroepOverzichtItem
import com.startspeler.dto.GroupOverviewItem
import mui.material.*
import mui.material.styles.TypographyVariant
import react.FC
import react.Props
import react.dom.onChange
import react.useState

external interface GroepenPageProps : Props {
    var groups: List<GroupOverviewItem>
    var loading: Boolean
    var error: String?
    var onAdd: ((name: String, discount: Float?) -> Unit)?
    var onEdit: ((id: Int, name: String, discount: Float?) -> Unit)?
    var onDelete: ((id: Int) -> Unit)?
}

val GroepenPage = FC<GroepenPageProps> { props ->
    val (dialogOpen, setDialogOpen) = useState(false)
    val (editGroup, setEditGroup) = useState<GroupOverviewItem?>(null)
    val (dialogName, setDialogName) = useState("")
    val (dialogDiscount, setDialogDiscount) = useState("")
    val (discountError, setDiscountError) = useState<String?>(null)

    fun openAdd() {
        setEditGroup(null)
        setDialogName("")
        setDialogDiscount("")
        setDiscountError(null)
        setDialogOpen(true)
    }

    fun openEdit(group: GroupOverviewItem) {
        setEditGroup(group)
        setDialogName(group.name)
        setDialogDiscount(if (group.discountPercentage != 0f) group.discountPercentage.toString() else "")
        setDiscountError(null)
        setDialogOpen(true)
    }

    fun submitDialog() {
        val name = dialogName.trim()
        if (name.isEmpty()) return
        val discountRaw = dialogDiscount.trim()
        val discount = if (discountRaw.isEmpty()) null else discountRaw.toFloatOrNull()
        if (discountRaw.isNotEmpty()) {
            if (discount == null || discount < 0f || discount > 100f) {
                setDiscountError("Korting moet tussen 0 en 100 zijn")
                return
            }
        }
        setDiscountError(null)
        val eg = editGroup
        if (eg == null) {
            props.onAdd?.invoke(name, discount)
        } else {
            props.onEdit?.invoke(eg.id, name, discount)
        }
        setDialogOpen(false)
    }

    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '12px', padding: '16px', width: '100%', boxSizing: 'border-box' }")

        Box {
            sx = js("{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }")
            Typography {
                variant = TypographyVariant.h5
                sx = js("{ color: '#2B3078', fontWeight: 700 }")
                +"Community"
            }
            Button {
                variant = ButtonVariant.contained
                color = ButtonColor.primary
                size = Size.small
                asDynamic().className = "btnPrimary"
                onClick = { openAdd() }
                +"Toevoegen"
            }
        }

        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: '6px 16px', borderBottom: '1px solid #e0e0e0', background: '#fafafa', fontSize: '0.95rem', fontWeight: 600, color: '#666', gap: '16px' }")
            Typography { sx = js("{ width: '220px', minWidth: '220px', maxWidth: '220px' }"); +"Community" }
            Typography { sx = js("{ width: '140px', minWidth: '140px', maxWidth: '140px' }"); +"Aantal personen" }
            Typography { sx = js("{ width: '140px', minWidth: '140px', maxWidth: '140px' }"); +"Korting" }
            Typography { sx = js("{ marginLeft: 'auto', paddingRight: '52px' }"); +"Acties" }
        }

        if (props.loading) {
            CircularProgress {}
        } else if (props.error != null) {
            Typography {
                sx = js("{ color: '#d32f2f' }")
                +props.error!!
            }
        } else {
            val sortedGroups = props.groups.sortedWith(compareBy({ if (it.id == 1) 0 else 1 }, { it.name }))
            sortedGroups.forEach { group ->
                GroepOverzichtItem {
                    this.group = group
                    this.onEdit = { openEdit(group) }
                    this.onDelete = { props.onDelete?.invoke(group.id) }
                }
            }
        }
    }

    // Add/Edit Dialog
    Dialog {
        open = dialogOpen
        onClose = { _, _ -> setDialogOpen(false) }
        DialogTitle { +(if (editGroup == null) "Community toevoegen" else "Community aanpassen") }
        DialogContent {
            TextField {
                label = react.ReactNode("Naam")
                value = dialogName
                onChange = { e -> setDialogName(e.target.asDynamic().value as String) }
                fullWidth = true
                sx = js("{ marginBottom: '16px', marginTop: '8px' }")
            }
            TextField {
                label = react.ReactNode("Korting (%)")
                value = dialogDiscount
                onChange = { e -> setDialogDiscount(e.target.asDynamic().value as String) }
                fullWidth = true
                asDynamic().type = "number"
                asDynamic().inputProps = js("{ min: 0, max: 100, step: 0.5 }")
                error = discountError != null
                helperText = react.ReactNode(discountError ?: "")
            }
        }
        DialogActions {
            Button {
                onClick = { setDialogOpen(false) }
                +"Annuleren"
            }
            Button {
                variant = ButtonVariant.contained
                onClick = { submitDialog() }
                +(if (editGroup == null) "Toevoegen" else "Opslaan")
            }
        }
    }
}
