package com.startspeler.ui

import com.startspeler.dto.GroepItem
import com.startspeler.models.Group
import mui.material.*
import react.FC
import react.Props

external interface GroepListProps : Props {
    var groepen: List<Group>
    var onDetails: (Group) -> Unit
    var onEdit: (Group) -> Unit
    var onDelete: (Group) -> Unit
}

val GroepList = FC<GroepListProps> { props ->
    TableContainer {
        Table {
            TableHead {
                TableRow {
                    TableCell { +"Naam" }
                    TableCell { +"Korting" }
                    TableCell { +"Details" }
                    TableCell { +"Bewerken" }
                    TableCell { +"Verwijderen" }
                }
            }

            TableBody {
                props.groepen.forEach { groep ->
                    val groepItem = GroepItem(
                        id = groep.id,
                        name = groep.name,
                        discount = groep.discount,
                        memberCount = groep.memberCount
                    )
                    GroepRow {
                        this.groep = groepItem
                        this.onDetails = props.onDetails
                        this.onEdit = props.onEdit
                        this.onDelete = { props.onDelete(groep) }
                    }
                }
            }
        }
    }
}