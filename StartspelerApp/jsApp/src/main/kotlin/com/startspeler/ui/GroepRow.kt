package com.startspeler.ui

import com.startspeler.dto.GroepItem
import com.startspeler.models.Group
import mui.material.Button
import mui.material.TableCell
import mui.material.TableRow
import react.FC
import react.Props

external interface GroepRowProps : Props {
    var groep: GroepItem
    var onDetails: (Group) -> Unit
    var onEdit: (Group) -> Unit
    var onDelete: (Group) -> Unit
    var onGroepClick: (Group) -> Unit
}


val GroepRow = FC<GroepRowProps> { props ->

    // Convert GroepItem → Group (your real model)
    val groep = Group(
        id = props.groep.id.toInt(),
        name = props.groep.name,
        discount = props.groep.discount,
        memberCount = props.groep.memberCount
    )

    TableRow {
        onClick = { props.onGroepClick(groep) }

        TableCell { +props.groep.name }
        TableCell { +"${props.groep.discount}%" }
        TableCell { +"${props.groep.memberCount} leden" }

        TableCell {
            Button {
                onClick = { props.onDetails(groep) }
                +"Details"
            }
        }

        TableCell {
            Button {
                onClick = { props.onEdit(groep) }
                +"Bewerken"
            }
        }

        TableCell {
            Button {
                onClick = { props.onDelete(groep) }
                +"Verwijderen"
            }
        }
    }
}