package com.startspeler.components.groepen

import com.startspeler.dto.GroupOverviewItem
import mui.icons.material.ExpandMore
import mui.material.Accordion
import mui.material.AccordionDetails
import mui.material.AccordionSummary
import mui.material.Box
import mui.material.Button
import mui.material.Typography
import react.FC
import react.Props
import react.create
import react.useState

external interface GroepOverzichtItemProps : Props {
    var group: GroupOverviewItem
}

val GroepOverzichtItem = FC<GroepOverzichtItemProps> { props ->
    val group = props.group
    val (isOpen, setOpen) = useState(false)

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
                    sx = js("{ marginLeft: 'auto', paddingRight: '12px' }")
                    Button {
                        variant = mui.material.ButtonVariant.outlined
                        color = mui.material.ButtonColor.primary
                        size = mui.material.Size.small
                        onClick = { event ->
                            event.stopPropagation()
                        }
                        +"Aanpassen"
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
                    +"Personen in deze groep"
                }
                if (group.members.isEmpty()) {
                    Typography {
                        sx = js("{ fontSize: '0.9rem', color: '#666' }")
                        +"Er zitten momenteel geen personen in deze groep."
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
}
