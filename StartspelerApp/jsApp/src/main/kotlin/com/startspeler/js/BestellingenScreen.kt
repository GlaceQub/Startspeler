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

    // Bereken vandaag als default datum
    fun todayString(): String {
        val now = js("new Date()")
        val y = now.getFullYear() as Int
        val m = ((now.getMonth() as Int) + 1).toString().padStart(2, '0')
        val d = (now.getDate() as Int).toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    // Bereken van/tot: tot = geselecteerde dag 23:59, van = exact 48u eerder
    fun computeFromTo(date: String): Pair<String, String> {
        val to = "${date}T23:59"
        val jsDateFrom = js("new Date(new Date(date + 'T23:59:00').getTime() - 48 * 60 * 60 * 1000)")
        val fy = jsDateFrom.getFullYear() as Int
        val fm = ((jsDateFrom.getMonth() as Int) + 1).toString().padStart(2, '0')
        val fd = (jsDateFrom.getDate() as Int).toString().padStart(2, '0')
        val fh = (jsDateFrom.getHours() as Int).toString().padStart(2, '0')
        val fmin = (jsDateFrom.getMinutes() as Int).toString().padStart(2, '0')
        val from = "$fy-$fm-${fd}T$fh:$fmin"
        return Pair(from, to)
    }

    val defaultDate = todayString()
    val defaultFromTo = computeFromTo(defaultDate)

    val (selectedDate, setSelectedDate) = useState(defaultDate)
    val (dateFrom, setDateFrom) = useState(defaultFromTo.first)
    val (dateTo, setDateTo) = useState(defaultFromTo.second)

    val handleSelectedDateChange: (String) -> Unit = { date ->
        setSelectedDate(date)
        if (date.isNotEmpty()) {
            val (from, to) = computeFromTo(date)
            setDateFrom(from)
            setDateTo(to)
        } else {
            setDateFrom("")
            setDateTo("")
        }
    }

    suspend fun fetchOrders(from: String = dateFrom, to: String = dateTo) {
        setLoading(true)
        setError(null)
        try {
            val configResponse = window.fetch("/config.json").await()
            val config = configResponse.json().await().asDynamic()
            val backendUrl = config.backendUrl as? String ?: ""
            val params = buildString {
                val parts = mutableListOf<String>()
                if (from.isNotEmpty()) parts.add("from=$from")
                if (to.isNotEmpty()) parts.add("to=$to")
                if (parts.isNotEmpty()) append("?${parts.joinToString("&")}")
            }
            val response = window.fetch("$backendUrl/order/all$params").await()
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

    useEffectOnce {
        MainScope().launch {
            fetchOrders(defaultFromTo.first, defaultFromTo.second)
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
        this.selectedDate = selectedDate
        this.onSelectedDateChange = handleSelectedDateChange
        this.onApplyDateFilter = { MainScope().launch { fetchOrders(dateFrom, dateTo) } }
        this.onCheckoutSuccess = { MainScope().launch { fetchOrders(dateFrom, dateTo) } }
    }
}