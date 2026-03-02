package com.startspeler.js

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

    // snackbar for feedback (zoals BestelScreen)
    val (snackbarOpen, setSnackbarOpen) = useState(false)
    val (snackbarMsg, setSnackbarMsg) = useState("")
    val (snackbarSeverity, setSnackbarSeverity) = useState("info") // "success" | "error" | "info"

    fun showToast(msg: String, severity: String = "info") {
        setSnackbarMsg(msg)
        setSnackbarSeverity(severity)
        setSnackbarOpen(true)
    }

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

    // Load backendUrl from config.json on mount (zelfde als BestelScreen/InventoryScreen)
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

    // Fetch tafels when backendUrl loaded
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

    val handleToggleActive: (Int, Boolean) -> Unit = { id, newActive ->
        val url = backendUrl
        if (url == null) {
            showToast("Backend URL niet geladen", "error")
        }else{

        val newStatusId = if (newActive) activeStatusId else inactiveStatusId

        scope.launch {
            try {
                val endpoint = url.trimEnd('/') + "/tafels/$id/status"
                val init: dynamic = js("{}")
                init.method = "PATCH"
                init.headers = js("{ 'Content-Type': 'application/json' }")
                init.body = json.encodeToString(TafelStatusUpdateDto(statusId = newStatusId))

                val resp = window.fetch(endpoint, init).await()
                val respText = resp.text().await()

                if (resp.ok) {
                    val updated = json.decodeFromString(TafelDto.serializer(), respText)
                    setTafels(tafels.map { if (it.id == updated.id) updated else it })
                    showToast("Tafel bijgewerkt", "success")
                } else {
                    showToast("Status wijzigen mislukt: ${resp.status} ${resp.statusText}", "error")
                }
            } catch (e: Throwable) {
                showToast("Fout bij status wijzigen: $e", "error")
            }
        }
    }
    }

    TafelPage {
        this.items = tafels.map(::dtoToView)
        this.loading = loading
        this.error = error
        this.onToggleActive = handleToggleActive
    }

    Snackbar {
        open = snackbarOpen
        autoHideDuration = 3000
        onClose = { _, _ -> setSnackbarOpen(false) }
        Alert {
            // severity verwacht dynamic/string in Kotlin wrappers
            asDynamic().severity = snackbarSeverity
            sx = js("{ width: '100%' }")
            +snackbarMsg
        }
    }
}