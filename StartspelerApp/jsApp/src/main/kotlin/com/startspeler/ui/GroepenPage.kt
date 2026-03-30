package com.startspeler.ui

import com.startspeler.components.groepen.GroepOverzichtItem
import com.startspeler.dto.GroupOverviewItem
import mui.material.Box
import mui.material.Button
import mui.material.CircularProgress
import mui.material.Typography
import react.FC
import react.Props

external interface GroepenPageProps : Props {
    var groups: List<GroupOverviewItem>
    var loading: Boolean
    var error: String?
}

val GroepenPage = FC<GroepenPageProps> { props ->
    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '12px', padding: '16px', width: '100%', boxSizing: 'border-box' }")

        Box {
            sx = js("{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h5
                sx = js("{ color: '#2B3078', fontWeight: 700 }")
                +"Groepen"
            }
            Button {
                variant = mui.material.ButtonVariant.contained
                color = mui.material.ButtonColor.primary
                size = mui.material.Size.small
                +"Toevoegen"
            }
        }

        Box {
            sx = js("{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: '6px 16px', borderBottom: '1px solid #e0e0e0', background: '#fafafa', fontSize: '0.95rem', fontWeight: 600, color: '#666', gap: '16px' }")
            Typography { sx = js("{ width: '220px', minWidth: '220px', maxWidth: '220px' }"); +"Groep" }
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
            props.groups.forEach { group ->
                GroepOverzichtItem {
                    this.group = group
                }
            }
        }
    }
}
