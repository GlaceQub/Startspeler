package com.startspeler.js

import com.startspeler.dto.GroupCreate
import com.startspeler.dto.GroupOverviewItem
import com.startspeler.dto.GroupUpdate
import com.startspeler.ui.GroepenPage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.useEffect
import react.useEffectOnce
import react.useState

private val scope = MainScope()
private val json = Json { ignoreUnknownKeys = true }

val GroepenScreen = FC<Props> {
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val (groups, setGroups) = useState<List<GroupOverviewItem>>(emptyList())
    val (loading, setLoading) = useState(true)
    val (error, setError) = useState<String?>(null)

    fun loadGroups(url: String) {
        setLoading(true)
        scope.launch {
            try {
                val resp = window.fetch(url.trimEnd('/') + "/groups").await()
                val text = resp.text().await()
                if (!resp.ok) {
                    setError("Groepen ophalen mislukt: ${resp.status} ${resp.statusText}")
                    setGroups(emptyList())
                } else {
                    val list = json.decodeFromString(ListSerializer(GroupOverviewItem.serializer()), text)
                    setGroups(list)
                }
            } catch (e: Throwable) {
                setError("Fout bij het ophalen van de groepen: $e")
            } finally {
                setLoading(false)
            }
        }
    }

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

    useEffect(dependencies = arrayOf(backendUrl)) {
        val url = backendUrl ?: return@useEffect
        loadGroups(url)
    }

    val handleAdd: (String, Float?) -> Unit = add@{ name, discount ->
        val url = backendUrl ?: run { setError("Backend URL niet geladen"); return@add }
        scope.launch {
            try {
                val endpoint = url.trimEnd('/') + "/groups"
                val init: dynamic = js("{}")
                init.method = "POST"
                init.headers = js("{ 'Content-Type': 'application/json' }")
                init.body = json.encodeToString(GroupCreate(name = name, discount = discount))
                val resp = window.fetch(endpoint, init).await()
                if (resp.ok) loadGroups(url)
                else setError("Toevoegen mislukt: ${resp.status} ${resp.statusText}")
            } catch (e: Throwable) {
                setError("Fout bij toevoegen: $e")
            }
        }
    }

    val handleEdit: (Int, String, Float?) -> Unit = edit@{ id, name, discount ->
        val url = backendUrl ?: run { setError("Backend URL niet geladen"); return@edit }
        scope.launch {
            try {
                val endpoint = url.trimEnd('/') + "/groups/$id"
                val init: dynamic = js("{}")
                init.method = "PUT"
                init.headers = js("{ 'Content-Type': 'application/json' }")
                init.body = json.encodeToString(GroupUpdate(name = name, discount = discount))
                val resp = window.fetch(endpoint, init).await()
                if (resp.ok) loadGroups(url)
                else setError("Bijwerken mislukt: ${resp.status} ${resp.statusText}")
            } catch (e: Throwable) {
                setError("Fout bij bijwerken: $e")
            }
        }
    }

    val handleDelete: (Int) -> Unit = delete@{ id ->
        val url = backendUrl ?: return@delete
        scope.launch {
            try {
                val endpoint = url.trimEnd('/') + "/groups/$id"
                val init: dynamic = js("{}")
                init.method = "DELETE"
                val resp = window.fetch(endpoint, init).await()
                if (resp.ok || resp.status.toInt() == 204) loadGroups(url)
                else setError("Verwijderen mislukt: ${resp.status} ${resp.statusText}")
            } catch (e: Throwable) {
                setError("Fout bij verwijderen: $e")
            }
        }
    }

    GroepenPage {
        this.groups = groups
        this.loading = loading
        this.error = error
        this.onAdd = handleAdd
        this.onEdit = handleEdit
        this.onDelete = handleDelete
    }
}
