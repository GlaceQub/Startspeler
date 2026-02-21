package com.startspeler.js

import com.startspeler.dto.OrderOverzichtItem
import com.startspeler.ui.BestellingenPage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.useEffectOnce
import react.useState

val BestellingenScreen = FC<Props> {
    val (orders, setOrders) = useState<List<OrderOverzichtItem>>(emptyList())
    val (loading, setLoading) = useState(true)
    val (error, setError) = useState<String?>(null)
    val (filter, setFilter) = useState("")

    val statusOptions = listOf("aangemaakt", "in behandeling", "betaald")
    val (selectedStatuses, setSelectedStatuses) = useState<List<String>>(listOf("aangemaakt", "in behandeling"))
    val handleStatusChange: (List<String>) -> Unit = { setSelectedStatuses(it) }

    val handleFilterChange: (String) -> Unit = { setFilter(it) }

    useEffectOnce {
        MainScope().launch {
            try {
                val configResponse = window.fetch("/config.json").await()
                val config = configResponse.json().await().asDynamic()
                val backendUrl = config.backendUrl as? String ?: ""

                val response = window.fetch("$backendUrl/order/all").await()
                if (response.ok) {
                    val json = Json { ignoreUnknownKeys = true }
                    val text = response.text().await()
                    val parsed = json.decodeFromString<List<OrderOverzichtItem>>(text)
                    setOrders(parsed)
                } else {
                    setError("Fout bij ophalen orders: ${response.status}")
                }
            } catch (e: Throwable) {
                setError("Fout: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    BestellingenPage {
        this.orders = orders
        this.loading = loading
        this.error = error
        this.filter = filter
        this.onFilterChange = handleFilterChange
        this.statusOptions = statusOptions
        this.selectedStatuses = selectedStatuses
        this.onStatusChange = handleStatusChange
    }
}