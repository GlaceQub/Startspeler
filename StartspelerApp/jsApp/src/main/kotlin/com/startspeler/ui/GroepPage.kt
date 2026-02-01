package com.startspeler.ui

import com.startspeler.models.Group
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Box
import react.FC
import react.Props

external interface GroepPageProps : Props {
    var groepen: List<Group>
    var onAddGroep: () -> Unit
    var onRemoveGroep: (Group) -> Unit
    var onEditGroep: (Group) -> Unit
    var onDetailsGroep: (Group) -> Unit
}

val GroepPage = FC<GroepPageProps> { props ->
    Box {
        sx = js("""
            {
                display: 'flex',
                flexDirection: 'column',
                gap: '24px',
                padding: '24px',
                width: '100%',
                boxSizing: 'border-box'
            }
        """)

        Typography {
            variant = TypographyVariant.h5
            +"Groepenbeheer"
        }


        Button {
            variant = ButtonVariant.contained
            color = ButtonColor.primary
            onClick = { props.onAddGroep() }
            +"Nieuwe groep toevoegen"
        }

        GroepList {
            this.groepen = props.groepen
            this.onDetails = props.onDetailsGroep
            this.onEdit = props.onEditGroep
            this.onDelete = { groep ->
                props.onRemoveGroep(groep)   // ← forward to GroepScreen
            }


        }
    }
}