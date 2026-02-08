package com.startspeler.components.account

import com.startspeler.dto.klantToevoegen
import mui.material.Button
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.TextField
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange
import react.useState

external interface AddKlantModalProps : Props {
    var open: Boolean
    var onClose: () -> Unit
    var onAdd: (String, String?) -> Unit // name, email
    var existingNames: List<String>
    var existingEmails: List<String>
}

val AddKlantModal = FC<AddKlantModalProps> { props ->
    val (klant, setKlant) = useState(klantToevoegen())
    val nameExists = props.existingNames.any { it.equals(klant.naam.trim(), ignoreCase = true) }
    val emailExists = klant.email?.let { email ->
        email.isNotEmpty() && props.existingEmails.any { it.equals(email.trim(), ignoreCase = true) }
    } ?: false

    Dialog {
        open = props.open
        onClose = { _, _ -> props.onClose() }
        DialogTitle { +"Nieuwe klant toevoegen" }
        DialogContent {
            mui.system.Box {
                sx = js("{ display: 'flex', flexDirection: 'column', gap: '16px' }")
                TextField {
                    label = ReactNode("Naam")
                    value = klant.naam
                    onChange = { event ->
                        val value = event.target.asDynamic().value as String
                        val newKlant = klantToevoegen()
                        newKlant.naam = value
                        newKlant.email = klant.email
                        setKlant(newKlant)
                    }
                    fullWidth = true
                    error = nameExists
                    helperText = if (nameExists) ReactNode("Deze naam bestaat al") else null
                }
                TextField {
                    label = ReactNode("Email (optioneel)")
                    value = klant.email ?: ""
                    onChange = { event ->
                        val value = event.target.asDynamic().value as String
                        val newKlant = klantToevoegen()
                        newKlant.naam = klant.naam
                        newKlant.email = value
                        setKlant(newKlant)
                    }
                    fullWidth = true
                    error = emailExists
                    helperText = if (emailExists) ReactNode("Dit emailadres bestaat al") else ReactNode("Optioneel")
                }
            }
        }
        DialogActions {
            Button {
                onClick = { props.onClose() }
                +"Annuleren"
            }
            Button {
                onClick = {
                    props.onAdd(klant.naam.trim(), klant.email?.trim().takeIf { !it.isNullOrEmpty() })
                    setKlant(klantToevoegen())
                    props.onClose()
                }
                disabled = klant.naam.trim().isEmpty() || nameExists || emailExists
                +"Toevoegen"
            }
        }
    }
}