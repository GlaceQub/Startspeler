package com.startspeler.js

import com.startspeler.klanten.dto.KlantUpdateRequestDto
import com.startspeler.klanten.dto.KlantenDto
import com.startspeler.ui.KlantView
import com.startspeler.ui.KlantenPage
import kotlinx.browser.window
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.w3c.fetch.Response
import react.FC
import react.Props
import react.useEffect
import react.useState
import kotlin.js.Promise

private val json = Json { ignoreUnknownKeys = true }


private const val BACKEND_BASE_URL = "http://localhost:8080"
private fun textPromise(resp: Response): Promise<String> =
    resp.text().unsafeCast<Promise<String>>()

external interface KlantenScreenProps : Props

val KlantenScreen = FC<KlantenScreenProps> {
    val (items, setItems) = useState<List<KlantView>>(emptyList())
    val (loading, setLoading) = useState(false)
    val (error, setError) = useState<String?>(null)
    val (initialized, setInitialized) = useState(false)

    fun load(name: String = "", email: String = "") {
        val params = mutableListOf<String>()
        if (name.isNotBlank()) params += "name=" + encodeURIComponent(name)
        if (email.isNotBlank()) params += "email=" + encodeURIComponent(email)
        val qs = if (params.isEmpty()) "" else "?" + params.joinToString("&")
        val url = "$BACKEND_BASE_URL/klanten$qs"

        setLoading(true)
        setError(null)

        window.fetch(url)
            .then { resp: Response ->
                textPromise(resp).then { text -> Pair(resp, text) }
            }
            .then { (resp: Response, text: String) ->
                if (!resp.ok) {
                    setError("Load failed: ${resp.status} ${resp.statusText} $text")
                    setItems(emptyList())
                } else {
                    val dtos = json.decodeFromString(ListSerializer(KlantenDto.serializer()), text)
                    setItems(dtos.map { dto ->
                        js("({})").unsafeCast<KlantView>().also { v ->
                            v.id = dto.id
                            v.name = dto.name
                            v.email = dto.email
                            v.groupId = dto.groupId
                            v.roleId = dto.roleId
                            v.statusId = dto.statusId
                        }
                    })
                }
            }
            .catch { e ->
                setError("Error loading: $e")
                setItems(emptyList())
            }
            .finally {
                setLoading(false)
            }
    }

    fun update(id: Int, name: String, email: String?, groupId: Int?) {
        setError(null)
        val init: dynamic = js("{}")
        init.method = "PUT"
        init.headers = js("{ 'Content-Type': 'application/json' }")
        init.body = json.encodeToString(
            KlantUpdateRequestDto.serializer(),
            KlantUpdateRequestDto(name = name, email = email, groupId = groupId)
        )

        window.fetch("$BACKEND_BASE_URL/klanten/$id", init)
            .then { resp: Response ->
                textPromise(resp).then { text -> Pair(resp, text) }
            }
            .then { (resp: Response, text: String) ->
                if (!resp.ok) setError("Save failed: ${resp.status} ${resp.statusText} $text")
                else load()
            }
            .catch { e -> setError("Error saving: $e") }
    }

    fun delete(id: Int) {
        setError(null)
        val init: dynamic = js("{}")
        init.method = "DELETE"

        window.fetch("$BACKEND_BASE_URL/klanten/$id", init)
            .then { resp: Response ->
                textPromise(resp).then { text -> Pair(resp, text) }
            }
            .then { (resp: Response, text: String) ->
                if (resp.ok || resp.status.toInt() == 204) load()
                else setError("Delete failed: ${resp.status} ${resp.statusText} $text")
            }
            .catch { e -> setError("Error deleting: $e") }
    }

    useEffect {
        if (!initialized) {
            setInitialized(true)
            load()
        }
    }

    KlantenPage {
        this.items = items
        this.loading = loading
        this.error = error
        this.onSearch = { n, e -> load(n, e) }
        this.onUpdate = { id, n, e, g -> update(id, n, e, g) }
        this.onDelete = { id -> delete(id) }
    }
}

private fun encodeURIComponent(value: String): String =
    js("encodeURIComponent")(value) as String
