package com.startspeler.js

import com.startspeler.tables.dto.TafelCreate
import com.startspeler.tables.dto.TafelUpdate
import com.startspeler.ui.TafelPage
import com.startspeler.ui.TafelView
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mui.material.Alert
import mui.material.Snackbar
import react.FC
import react.Props
import react.useEffect
import react.useEffectOnce
import react.useState

external interface TafelScreenProps : Props

private val scope = MainScope()
private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class TafelDto(
    val id: Int,
    val number: Int,
    val statusId: Int,
    val statusName: String
)
@Serializable
data class TafelStatusUpdateDto(val statusId: Int)

val TafelScreen = FC<TafelScreenProps> {
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val (error, setError) = useState<String?>(null)
    val (loading, setLoading) = useState(false)

    val (tafels, setTafels) = useState<List<TafelDto>>(emptyList())

    val (snackbarOpen, setSnackbarOpen) = useState(false)
    val (snackbarMsg, setSnackbarMsg) = useState("")
    val (snackbarSeverity, setSnackbarSeverity) = useState("info") // "success" | "error" | "info"

    fun loadTafels(url: String) {
        setLoading(true)
        scope.launch {
            try {
                val resp = window.fetch(url.trimEnd('/') + "/tafels/all").await()
                val text = resp.text().await()
                if (!resp.ok) {
                    setError("Tafels ophalen mislukt: ${resp.status} ${resp.statusText}")
                    setTafels(emptyList())
                } else {
                    val list = json.decodeFromString(ListSerializer(TafelDto.serializer()), text)
                    setTafels(list)
                }
            } catch (e: Throwable) {
                setError("Fout bij het ophalen van de tafels: $e")
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
        loadTafels(url)
    }

    val activeStatusId = 1
    val inactiveStatusId = 2

    fun dtoToView(dto: TafelDto): TafelView {
        val obj: dynamic = js("{}")
        obj.id = dto.id
        obj.number = dto.number
        obj.active = dto.statusId == activeStatusId
        return obj.unsafeCast<TafelView>()
    }

    val handleToggleActive: (Int, Boolean) -> Unit = toggle@{ id, newActive ->
        val url = backendUrl ?: run {
            setSnackbarMsg("Backend URL niet geladen")
            setSnackbarSeverity("error")
            setSnackbarOpen(true)
            return@toggle
        }

        val tafel = tafels.firstOrNull { it.id == id } ?: run {
            setSnackbarMsg("Tafel niet gevonden")
            setSnackbarSeverity("error")
            setSnackbarOpen(true)
            return@toggle
        }

        val newStatusId = if (newActive) activeStatusId else inactiveStatusId

        scope.launch {
            try {
                val endpoint = url.trimEnd('/') + "/tafels/$id"
                val init: dynamic = js("{}")
                init.method = "PUT"
                init.headers = js("{ 'Content-Type': 'application/json' }")
                init.body = json.encodeToString(TafelUpdate(number = tafel.number, statusId = newStatusId))

                val resp = window.fetch(endpoint, init).await()
                val respText = resp.text().await()

                if (resp.ok) {
                    loadTafels(url)
                    setSnackbarMsg("Tafel bijgewerkt")
                    setSnackbarSeverity("success")
                    setSnackbarOpen(true)
                } else {
                    setSnackbarMsg("Bijwerken mislukt: ${resp.status} ${resp.statusText} $respText")
                    setSnackbarSeverity("error")
                    setSnackbarOpen(true)
                }
            } catch (e: Throwable) {
                setSnackbarMsg("Fout bij bijwerken: $e")
                setSnackbarSeverity("error")
                setSnackbarOpen(true)
            }
        }
    }


    val handleAdd: (Int) -> Unit = add@{ number ->
        val url = backendUrl
        if (url == null) {
            setSnackbarMsg("Backend URL niet geladen")
            setSnackbarSeverity("error")
            setSnackbarOpen(true)
            return@add
        }

        if (number <= 0) {
            setSnackbarMsg("Tafelnummer moet groter zijn dan 0")
            setSnackbarSeverity("error")
            setSnackbarOpen(true)
            return@add
        }

        scope.launch {
            try {
                val endpoint = url.trimEnd('/') + "/tafels"
                val init: dynamic = js("{}")
                init.method = "POST"
                init.headers = js("{ 'Content-Type': 'application/json' }")
                init.body = json.encodeToString(TafelCreate(number = number, statusId = inactiveStatusId))

                val resp = window.fetch(endpoint, init).await()
                val text = resp.text().await()

                if (resp.ok) {
                    setSnackbarMsg("Tafel toegevoegd")
                    setSnackbarSeverity("success")
                    setSnackbarOpen(true)
                    loadTafels(url)
                } else {
                    setSnackbarMsg("Toevoegen mislukt: ${resp.status} ${resp.statusText} $text")
                    setSnackbarSeverity("error")
                    setSnackbarOpen(true)
                }
            } catch (e: Throwable) {
                setSnackbarMsg("Fout bij toevoegen: $e")
                setSnackbarSeverity("error")
                setSnackbarOpen(true)
            }
        }
    }

    val handleDelete: (Int) -> Unit = delete@{ id ->
        val url = backendUrl ?: return@delete
        scope.launch {
            val endpoint = url.trimEnd('/') + "/tafels/$id"
            val init: dynamic = js("{}")
            init.method = "DELETE"
            val resp = window.fetch(endpoint, init).await()
            if (resp.ok || resp.status.toInt() == 204) loadTafels(url)
        }
    }



    TafelPage {
        this.items = tafels.map(::dtoToView)
        this.loading = loading
        this.error = error
        this.onToggleActive = handleToggleActive
        this.onAdd = handleAdd
        this.onDelete = handleDelete
    }

    Snackbar {
        open = snackbarOpen
        autoHideDuration = 3000
        onClose = { _, _ -> setSnackbarOpen(false) }
        Alert {
            asDynamic().severity = snackbarSeverity
            sx = js("{ width: '100%' }")
            +snackbarMsg
        }
    }
}