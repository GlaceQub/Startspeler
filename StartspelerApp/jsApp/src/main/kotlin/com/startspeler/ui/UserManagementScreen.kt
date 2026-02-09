package com.startspeler.ui

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.useEffect
import react.useState
import com.startspeler.api.Api
import com.startspeler.api.UserDto
import com.startspeler.api.UpdateUserRequestDto
import mui.material.*
import mui.icons.material.Edit
import mui.icons.material.Delete
import mui.icons.material.Group
import react.dom.html.ReactHTML.div
import org.w3c.dom.HTMLInputElement

private val scope = MainScope()

external interface UserManagementProps : Props {
    var baseUrl: String?
}

val UserManagementScreen = FC<UserManagementProps> { props ->
    val baseUrl = props.baseUrl ?: "http://localhost:8080"

    val (query, setQuery) = useState("")
    val (users, setUsers) = useState<List<UserDto>>(emptyList())
    val (loading, setLoading) = useState(false)
    val (page, setPage) = useState(1)
    val (limit, setLimit) = useState(25)

    val (selected, setSelected) = useState<UserDto?>(null)
    val (editOpen, setEditOpen) = useState(false)
    val (confirmOpen, setConfirmOpen) = useState(false)
    val (confirmCandidates, setConfirmCandidates) = useState<List<String>>(emptyList())
    val (snackbarMsg, setSnackbarMsg) = useState<String?>(null)
    val (snackbarSeverity, setSnackbarSeverity) = useState("success")

    fun showSnackbar(msg: String, severity: String = "success") {
        setSnackbarMsg(msg)
        setSnackbarSeverity(severity)
    }

    fun loadUsers() {
        setLoading(true)
        scope.launch {
            try {
                val list = Api.fetchUsers(baseUrl, query, limit, (page - 1) * limit)
                setUsers(list)
            } catch (e: Throwable) {
                showSnackbar("Failed to load users: ${e.message}", "error")
            } finally {
                setLoading(false)
            }
        }
    }

    useEffect(listOf(query, page, limit)) { loadUsers() }

    // actions
    fun onEdit(u: UserDto) {
        setSelected(u)
        setEditOpen(true)
    }

    fun onDelete(u: UserDto) {
        if (!window.confirm("Verwijder gebruiker ${u.name}?")) return
        scope.launch {
            try {
                Api.deleteUser(baseUrl, u.id)
                showSnackbar("Gebruiker verwijderd", "success")
                loadUsers()
            } catch (e: Throwable) {
                showSnackbar("Verwijderen mislukt: ${e.message}", "error")
            }
        }
    }

    fun onAssignGroup(u: UserDto) {
        val newGroup = (u.groupId % 5) + 1
        scope.launch {
            try {
                Api.assignGroup(baseUrl, u.id, newGroup)
                showSnackbar("Groep toegewezen", "success")
                loadUsers()
            } catch (e: Throwable) {
                showSnackbar("Assign failed: ${e.message}", "error")
            }
        }
    }

    fun onSaveEdit(confirmSimilar: Boolean = false) {
        val u = selected ?: return
        val payload = UpdateUserRequestDto(
            name = u.name,
            email = u.email,
            groupId = u.groupId,
            roleId = u.roleId,
            statusId = u.statusId,
            confirmSimilar = confirmSimilar
        )
        scope.launch {
            try {
                Api.updateUser(baseUrl, u.id, payload)
                showSnackbar("Gebruiker bijgewerkt", "success")
                setEditOpen(false)
                loadUsers()
            } catch (t: Throwable) {
                val ae = t as? com.startspeler.api.ApiException
                val body = ae?.body
                if (body != null && body.contains("similar_name")) {
                    val matchesRegex = """"candidates"\s*:\s*\[([^\]]*)]""".toRegex()
                    val found = matchesRegex.find(body)
                    val list = found?.groups?.get(1)?.value
                        ?.split(",")
                        ?.map { it.trim().trim('"') }
                        ?.filter { it.isNotBlank() } ?: emptyList()
                    setConfirmCandidates(list)
                    setConfirmOpen(true)
                } else if (body != null && body.contains("duplicate_name")) {
                    showSnackbar("Naam al in gebruik", "error")
                } else if (body != null && body.contains("duplicate_email")) {
                    showSnackbar("E-mail al in gebruik", "error")
                } else {
                    showSnackbar("Update failed: ${t.message}", "error")
                }
            }
        }
    }

    // helper to update selected fields
    fun updateSelected(updater: (UserDto) -> UserDto) {
        val s = selected ?: return
        setSelected(updater(s))
    }

    // Render
    div {
        asDynamic().className = "user-management-root"

        Box {
            asDynamic().display = "flex"
            asDynamic().gap = "12px"
            asDynamic().alignItems = "center"
            TextField {
                asDynamic().label = "Zoek klant"
                asDynamic().variant = "outlined"
                asDynamic().size = "small"
                asDynamic().value = query
                asDynamic().onChange = { e: dynamic ->
                    val v = (e.target as? HTMLInputElement)?.value ?: ""
                    setQuery(v)
                }
            }
            Button {
                asDynamic().variant = "contained"
                +"Zoeken"
                onClick = { loadUsers() }
            }
            Button {
                asDynamic().variant = "outlined"
                +"Ververs"
                onClick = { loadUsers() }
            }
            if (loading) CircularProgress { asDynamic().size = 20 }
        }

        Box { asDynamic().sx = js("{ mt: 2 }")
            TableContainer {
                asDynamic().component = Paper
                Table {
                    TableHead {
                        TableRow {
                            TableCell { +"ID" }
                            TableCell { +"Naam" }
                            TableCell { +"Email" }
                            TableCell { +"Groep" }
                            TableCell { +"Acties" }
                        }
                    }
                    TableBody {
                        users.forEach { u: UserDto ->
                            TableRow {
                                key = u.id.toString()
                                TableCell { +u.id.toString() }
                                TableCell { +u.name }
                                TableCell { +(u.email ?: "") }
                                TableCell { +u.groupId.toString() }
                                TableCell {
                                    IconButton { onClick = { onEdit(u) }; Edit() }
                                    IconButton { onClick = { onDelete(u) }; Delete() }
                                    IconButton { onClick = { onAssignGroup(u) }; Group() }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edit dialog
        Dialog {
            open = editOpen
            onClose = { _, _ -> setEditOpen(false) }
            DialogTitle { +"Gebruiker bewerken" }
            DialogContent {
                selected?.let { u ->
                    Stack { asDynamic().spacing = 2
                        TextField {
                            asDynamic().label = "Naam"
                            asDynamic().fullWidth = true
                            asDynamic().value = u.name
                            asDynamic().onChange = { e: dynamic ->
                                val v = (e.target as? HTMLInputElement)?.value ?: ""
                                updateSelected { it.copy(name = v) }
                            }
                        }
                        TextField {
                            asDynamic().label = "Email"
                            asDynamic().fullWidth = true
                            asDynamic().value = u.email ?: ""
                            asDynamic().onChange = { e: dynamic ->
                                val v = (e.target as? HTMLInputElement)?.value ?: ""
                                updateSelected { it.copy(email = if (v.isBlank()) null else v) }
                            }
                        }
                        TextField {
                            asDynamic().label = "GroepId"
                            asDynamic().type = "number"
                            asDynamic().fullWidth = true
                            asDynamic().value = u.groupId.toString()
                            asDynamic().onChange = { e: dynamic ->
                                val v = (e.target as? HTMLInputElement)?.value ?: u.groupId.toString()
                                updateSelected { it.copy(groupId = v.toIntOrNull() ?: u.groupId) }
                            }
                        }
                        TextField {
                            asDynamic().label = "RoleId"
                            asDynamic().type = "number"
                            asDynamic().fullWidth = true
                            asDynamic().value = u.roleId.toString()
                            asDynamic().onChange = { e: dynamic ->
                                val v = (e.target as? HTMLInputElement)?.value ?: u.roleId.toString()
                                updateSelected { it.copy(roleId = v.toIntOrNull() ?: u.roleId) }
                            }
                        }
                        TextField {
                            asDynamic().label = "StatusId"
                            asDynamic().type = "number"
                            asDynamic().fullWidth = true
                            asDynamic().value = u.statusId.toString()
                            asDynamic().onChange = { e: dynamic ->
                                val v = (e.target as? HTMLInputElement)?.value ?: u.statusId.toString()
                                updateSelected { it.copy(statusId = v.toIntOrNull() ?: u.statusId) }
                            }
                        }
                    }
                }
            }
            DialogActions {
                Button { onClick = { setEditOpen(false) }; +"Annuleren" }
                Button { asDynamic().variant = "contained"; onClick = { onSaveEdit(false) }; +"Opslaan" }
            }
        }

        // Confirm similar names dialog
        Dialog {
            open = confirmOpen
            onClose = { _, _ -> setConfirmOpen(false) }
            DialogTitle { +"Vergelijkbare namen gevonden" }
            DialogContent {
                +"De volgende vergelijkbare namen zijn gevonden:"
                confirmCandidates.forEach { c -> div { +c } }
                +"Weet je zeker dat je wilt doorgaan?"
            }
            DialogActions {
                Button { onClick = { setConfirmOpen(false) }; +"Nee" }
                Button { asDynamic().variant = "contained"; onClick = { setConfirmOpen(false); onSaveEdit(true) }; +"Ja, doorgaan" }
            }
        }

        // Snackbar
        snackbarMsg?.let { msg ->
            Snackbar {
                open = true
                autoHideDuration = 4000
                onClose = { _, _ -> setSnackbarMsg(null) }
                Alert { asDynamic().severity = snackbarSeverity; +msg }
            }
        }
    }
}