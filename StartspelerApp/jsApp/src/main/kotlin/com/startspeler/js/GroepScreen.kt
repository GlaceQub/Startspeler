package com.startspeler.js

import com.startspeler.components.groep.AddGroepModal
import com.startspeler.models.Group
import com.startspeler.ui.GroepPage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.useEffect
import react.useState
import react.useEffectOnce
import kotlin.js.JSON
import kotlin.js.json

external interface GroepScreenProps : Props

val GroepScreen = FC<GroepScreenProps> {

    val (groepen, setGroepen) = useState<List<Group>>(emptyList())
    val (selectedGroep, setSelectedGroep) = useState<Group?>(null)
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val (error, setError) = useState<String?>(null)
    val (loadingGroepen, setLoadingGroepen) = useState(true)
    val (addGroepModalOpen, setAddGroepModalOpen) = useState(false)

    // Load backend URL from config.json
    useEffectOnce {
        window.fetch("/config.json")
            .then { response ->
                if (!response.ok) {
                    // fallback to localhost for dev if config.json missing
                    console.warn("Failed to load config.json: ${'$'}{response.status} ${'$'}{response.statusText} — falling back to http://localhost:8080 for dev")
                    setError("Failed to load config.json: ${'$'}{response.status} ${'$'}{response.statusText}")
                    setBackendUrl("http://localhost:8080")
                    return@then
                }
                response.json().then { dataAny ->
                    val data = dataAny.unsafeCast<dynamic>()
                    setBackendUrl(data.backendUrl as? String ?: "http://localhost:8080")
                }
            }
            .catch { _ ->
                console.warn("Error loading config.json — falling back to http://localhost:8080 for dev")
                setError("Error loading config.json")
                setBackendUrl("http://localhost:8080")
            }
    }

    // Reusable loader so we can refresh after creating a group
    val loadGroepen: () -> Unit = label@{
        val url = backendUrl ?: return@label
        setLoadingGroepen(true)
        MainScope().launch {
            try {
                val resp = window.fetch(
                    url.trimEnd('/') + "/groepen/raw"
                ).await()

                if (!resp.ok) {
                    setError("Fout bij het ophalen van de groepen: ${'$'}{resp.status} ${'$'}{resp.statusText}")
                    return@launch
                }

                val responseText = resp.text().await()
                val fetchedGroups = Json.decodeFromString<List<Group>>(responseText)
                setGroepen(fetchedGroups)

            } catch (_: Throwable) {
                setError("Fout bij het ophalen van de groepen")
            } finally {
                setLoadingGroepen(false)
            }
        }
    }

    // Fetch groups when backendUrl is available
    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl != null) {
            loadGroepen()
        }
    }

    // Add group modal open
    val openAddGroepModal: () -> Unit = {
        setAddGroepModalOpen(true)
    }

    val closeAddGroepModal: () -> Unit = {
        setAddGroepModalOpen(false)
    }

    // Add group POST
    val handleAddGroepModal: (String, Float?) -> Unit = { name, discount ->
        if (name.isNotBlank() && backendUrl != null) {
            MainScope().launch {
                try {
                    val bodyObj = json("name" to name, "discount" to discount)

                    val requestInit = js("{}")
                    requestInit.method = "POST"
                    requestInit.headers = json("Content-Type" to "application/json")
                    requestInit.body = JSON.stringify(bodyObj)

                    val response = window.fetch(
                        backendUrl.trimEnd('/') + "/groepen/add",
                        requestInit
                    ).await()

                    if (response.ok) {
                        // Refresh the list from server so UI matches DB
                        loadGroepen()
                        // optional quick feedback
                        try { window.alert("Groep toegevoegd") } catch (_: Throwable) { console.info("Groep toegevoegd") }
                    } else {
                        val text = try { response.text().await() } catch (_: Throwable) { "" }
                        setError("Groep toevoegen mislukt: ${'$'}{response.status} ${'$'}{response.statusText} ${'$'}{text}")
                    }

                } catch (_: Throwable) {
                    setError("Groep toevoegen mislukt")
                } finally {
                    setAddGroepModalOpen(false)
                }
            }
        } else {
            setAddGroepModalOpen(false)
        }
    }

    // Remove group locally
    val handleRemoveGroep: (Group) -> Unit = { groep ->
        setGroepen(groepen.filterNot { it.id == groep.id })
        if (selectedGroep?.id == groep.id) {
            setSelectedGroep(null)
        }
    }

    // UI
    GroepPage {
        this.groepen = groepen
        this.onAddGroep = { openAddGroepModal() }
        this.onRemoveGroep = { groep -> handleRemoveGroep(groep) }

    }

    AddGroepModal {
        open = addGroepModalOpen
        onClose = closeAddGroepModal
        onAdd = handleAddGroepModal
        existingNames = groepen.map { it.name }
        existingDiscounts = groepen.map { it.discount }
    }
}