package com.startspeler.components.groepen

import com.startspeler.dto.GroupOverviewItem
import mui.icons.material.Delete
import mui.icons.material.ExpandMore
import mui.material.*
import react.FC
import react.Props
import react.create
import react.useState

external interface GroepOverzichtItemProps : Props {
    var group: GroupOverviewItem
    var onEdit: (() -> Unit)?
    var onDelete: (() -> Unit)?
}

val GroepOverzichtItem = FC<GroepOverzichtItemProps> { props ->
    val group = props.group
    val (isOpen, setOpen) = useState(false)
    val (showDeleteModal, setShowDeleteModal) = useState(false)

    Accordion {
        expanded = isOpen
        onChange = { _, expanded -> setOpen(expanded) }
        AccordionSummary {
            expandIcon = ExpandMore.create()
            Box {
                sx = js("{ display: 'flex', flexDirection: 'row', gap: '16px', alignItems: 'center', width: '100%' }")
                Typography {
                    sx = js("{ width: '220px', minWidth: '220px', maxWidth: '220px', fontSize: '0.95rem' }")
                    +group.name
                }
                Typography {
                    sx = js("{ width: '140px', minWidth: '140px', maxWidth: '140px', fontSize: '0.95rem' }")
                    +group.memberCount.toString()
                }
                Typography {
                    sx = js("{ width: '140px', minWidth: '140px', maxWidth: '140px', fontSize: '0.95rem' }")
                    +"${group.discountPercentage}%"
                }
                Box {
                    sx = js("{ marginLeft: 'auto', display: 'flex', gap: '8px', paddingRight: '12px' }")
                    Button {
                        variant = ButtonVariant.outlined
                        color = ButtonColor.primary
                        size = Size.small
                        onClick = { event ->
                            event.stopPropagation()
                            props.onEdit?.invoke()
                        }
                        +"Aanpassen"
                    }
                    if (group.id != 1) {
                        IconButton {
                            size = Size.small
                            color = IconButtonColor.error
                            onClick = { event ->
                                event.stopPropagation()
                                setShowDeleteModal(true)
                            }
                            Delete {}
                        }
                    }
                }
            }
        }
        AccordionDetails {
            sx = js("{ padding: '0' }")
            Box {
                sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '12px 16px', background: '#f5f7fa', borderRadius: '0 0 8px 8px' }")
                Typography {
                    sx = js("{ fontWeight: 600, fontSize: '0.95rem', color: '#2B3078' }")
                    +"Personen in deze community"
                }
                if (group.members.isEmpty()) {
                    Typography {
                        sx = js("{ fontSize: '0.9rem', color: '#666' }")
                        +"Er zitten momenteel geen personen in deze community."
                    }
                } else {
                    group.members.forEach { member ->
                        Typography {
                            sx = js("{ fontSize: '0.92rem', color: '#333' }")
                            +member.name
                        }
                    }
                }
            }
        }
    }

    // Delete bevestigingsmodal
    if (showDeleteModal) {
        Modal {
            open = true
            onClose = { _, _ -> setShowDeleteModal(false) }
            Box {
                sx = js("{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: 'white', padding: '24px', borderRadius: '8px', boxShadow: '0 8px 32px rgba(0,0,0,0.18)', minWidth: '320px' }")
                Typography {
                    sx = js("{ fontWeight: 600, fontSize: '1.1rem', marginBottom: '8px' }")
                    +"Community verwijderen"
                }
                Typography {
                    sx = js("{ marginBottom: '8px' }")
                    +"Weet u zeker dat u \"${group.name}\" wilt verwijderen?"
                }
                if (group.memberCount > 0) {
                    val persoonLabel = if (group.memberCount == 1) "persoon wordt" else "personen worden"
                    Typography {
                        sx = js("{ marginBottom: '16px', color: '#e65100', fontSize: '0.92rem' }")
                        +"Let op: ${group.memberCount} $persoonLabel automatisch naar de standaard community verplaatst."
                    }
                } else {
                    Box { sx = js("{ marginBottom: '16px' }") }
                }
                Box {
                    sx = js("{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }")
                    Button {
                        onClick = { setShowDeleteModal(false) }
                        +"Annuleren"
                    }
                    Button {
                        variant = ButtonVariant.contained
                        color = ButtonColor.error
                        onClick = {
                            setShowDeleteModal(false)
                            props.onDelete?.invoke()
                        }
                        +"Verwijderen"
                    }
                }
            }
        }
    }
}
