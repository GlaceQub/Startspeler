package com.startspeler.components.groep

import com.startspeler.dto.groepToevoegen
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange
import react.useState

external interface AddGroepModalProps : Props {
    var open: Boolean
    var onClose: () -> Unit
    var onAdd: (String, Float?) -> Unit // name, discount
    var existingNames: List<String>
    var existingDiscounts: List<Float?>
}

val AddGroepModal = FC<AddGroepModalProps> { props ->
    val (groep, setGroep) = useState(groepToevoegen())

    val nameExists = props.existingNames.any { it.equals(groep.name.trim(), ignoreCase = true) }
    val discountInvalid = groep.discount?.let { it < 0 || it > 100 } ?: false

    Dialog {
        open = props.open
        onClose = { _, _ -> props.onClose() }

        DialogTitle { +"Nieuwe groep toevoegen" }

        DialogContent {
            TextField {
                label = ReactNode("Naam")
                value = groep.name
                onChange = { event ->
                    val value = event.target.asDynamic().value as String
                    setGroep(groep.copy(name = value))
                }
                fullWidth = true
                error = nameExists
                helperText = if (nameExists) ReactNode("Deze naam bestaat al") else null
            }

            TextField {
                label = ReactNode("Korting (%)")
                value = groep.discount?.toString() ?: ""
                onChange = { event ->
                    val raw = event.target.asDynamic().value as String
                    setGroep(groep.copy(discount = raw.toFloatOrNull()))
                }
                fullWidth = true
                error = discountInvalid
                helperText = if (discountInvalid) ReactNode("Korting moet tussen 0 en 100 zijn") else ReactNode("Optioneel")
            }
        }

        DialogActions {
            Button {
                onClick = { props.onClose() }
                +"Annuleren"
            }

            Button {
                onClick = {
                    props.onAdd(groep.name.trim(), groep.discount)
                    setGroep(groepToevoegen())
                    props.onClose()
                }
                disabled = groep.name.trim().isEmpty() || nameExists || discountInvalid
                +"Toevoegen"
            }
        }
    }
}