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
                    setError("Failed to load config.json: ${response.status} ${response.statusText}")
                    return@then
                }
                response.json().then { dataAny ->
                    val data = dataAny.unsafeCast<dynamic>()
                    setBackendUrl(data.backendUrl as? String)
                }
            }
            .catch { throwable ->
                setError("Error loading config.json: $throwable")
            }
    }

    // Fetch groups when backendUrl is available
    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl != null) {
            setLoadingGroepen(true)
            MainScope().launch {
                try {
                    val responseText = window.fetch(
                        backendUrl.trimEnd('/') + "/groepen"
                    ).await().text().await()

                    val fetchedGroups = Json.decodeFromString<List<Group>>(responseText)
                    setGroepen(fetchedGroups)

                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de groepen: $e")
                } finally {
                    setLoadingGroepen(false)
                }
            }
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
                    val bodyObj = js("{ name: name, discount: discount }")

                    val requestInit = js(
                        """
                        {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(bodyObj)
                        }
                        """
                    )

                    val response = window.fetch(
                        backendUrl.trimEnd('/') + "/groep/add",
                        requestInit
                    ).await()

                    if (response.ok) {
                        val created = response.json().await().unsafeCast<Group>()
                        setGroepen(groepen + created)
                    } else {
                        setError("Groep toevoegen mislukt: ${response.status} ${response.statusText}")
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