package com.startspeler.js

import com.startspeler.dto.ClientOpenOrdersSummary
import com.startspeler.dto.DeleteOrderResponse
import com.startspeler.dto.OrderOverzichtItem
import com.startspeler.ui.BestellingenPage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import react.FC
import react.Props
import react.useEffect
import react.useEffectOnce
import react.useState

val BestellingenScreen = FC<Props> {
    val json = Json { ignoreUnknownKeys = true }
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val userRole = (window.localStorage.getItem("userRole") ?: "").lowercase()
    val jwtToken = window.localStorage.getItem("jwtToken")
    val canDeleteOrders = !jwtToken.isNullOrBlank() && (userRole == "medewerker" || userRole == "beheerder")
    val (orders, setOrders) = useState<List<OrderOverzichtItem>>(emptyList())
    val (loading, setLoading) = useState(true)
    val (error, setError) = useState<String?>(null)
    val (filter, setFilter) = useState("")
    val (clientOptions, setClientOptions) = useState<List<String>>(emptyList())
    val (selectedClient, setSelectedClient) = useState("")
    val (bulkSummary, setBulkSummary) = useState<ClientOpenOrdersSummary?>(null)
    val (bulkModalOpen, setBulkModalOpen) = useState(false)
    val (bulkLoading, setBulkLoading) = useState(false)
    val (bulkError, setBulkError) = useState<String?>(null)
    val (clientInputError, setClientInputError) = useState<String?>(null)

    val statusOptions = listOf("aangemaakt", "in behandeling", "afgeleverd", "betaald")
    val (selectedStatuses, setSelectedStatuses) = useState<List<String>>(listOf("aangemaakt", "in behandeling", "afgeleverd"))
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
        val url = backendUrl ?: return
        setLoading(true)
        setError(null)
        try {
            val params = buildString {
                val parts = mutableListOf<String>()
                if (from.isNotEmpty()) parts.add("from=$from")
                if (to.isNotEmpty()) parts.add("to=$to")
                if (parts.isNotEmpty()) append("?${parts.joinToString("&")}")
            }
            val response = window.fetch(url.trimEnd('/') + "/order/all$params").await()
            if (response.ok) {
                val text = response.text().await()
                setOrders(json.decodeFromString<List<OrderOverzichtItem>>(text))
            } else {
                setError("Fout bij ophalen orders: ${response.status}")
            }
        } catch (e: Throwable) {
            setError("Fout: ${e.message}")
        } finally {
            setLoading(false)
        }
    }

    suspend fun fetchClients() {
        val url = backendUrl ?: return
        try {
            val response = window.fetch(url.trimEnd('/') + "/klanten").await()
            if (response.ok) {
                val text = response.text().await()
                val arr = JSON.parse<Array<dynamic>>(text)
                setClientOptions(arr.map { it.name as String })
            }
        } catch (_: Throwable) {
        }
    }

    fun patchOrder(updatedOrder: OrderOverzichtItem) {
        setOrders { currentOrders ->
            currentOrders.map { if (it.id == updatedOrder.id) updatedOrder else it }
        }
    }

    suspend fun moveOrderStatus(orderId: Int, direction: String) {
        val url = backendUrl ?: return
        setError(null)
        try {
            val response = window.fetch(
                url.trimEnd('/') + "/order/$orderId/status/$direction",
                js("{ method: 'POST' }")
            ).await()
            if (response.ok) {
                val updatedText = response.text().await()
                val updated = json.decodeFromString<com.startspeler.dto.OrderStatusTransitionResponse>(updatedText)
                updated.order?.let { patchOrder(it) }
            } else {
                setError("Status kon niet aangepast worden")
            }
        } catch (e: Throwable) {
            setError(e.message)
        }
    }

    suspend fun openBulkCheckoutSummaryForClient(clientName: String) {
        val url = backendUrl ?: return
        val trimmedClient = clientName.trim()
        if (trimmedClient.isBlank()) {
            setClientInputError("Selecteer of typ een bestaande klant")
            return
        }
        if (clientOptions.none { it.equals(trimmedClient, ignoreCase = true) }) {
            setClientInputError("Kies een bestaande klant uit de suggesties")
            return
        }
        setSelectedClient(trimmedClient)
        setClientInputError(null)
        setBulkLoading(true)
        setBulkError(null)
        try {
            val response = window.fetch(url.trimEnd('/') + "/order/open-by-client?clientName=$trimmedClient").await()
            if (response.ok) {
                val text = response.text().await()
                setBulkSummary(json.decodeFromString<ClientOpenOrdersSummary>(text))
                setBulkModalOpen(true)
            } else {
                setBulkError("Geen openstaande bestellingen gevonden")
            }
        } catch (e: Throwable) {
            setBulkError(e.message)
        } finally {
            setBulkLoading(false)
        }
    }

    suspend fun openBulkCheckoutSummary() {
        openBulkCheckoutSummaryForClient(selectedClient)
    }

    suspend fun confirmBulkCheckout(fixedDiscountAmount: Float?) {
        val url = backendUrl ?: return
        if (selectedClient.isBlank()) return
        setBulkLoading(true)
        setBulkError(null)
        try {
            val requestBody: dynamic = js("({})")
            requestBody.clientName = selectedClient
            requestBody.fixedDiscountAmount = fixedDiscountAmount
            val response = window.fetch(
                url.trimEnd('/') + "/order/checkout-client",
                js("""
                    ({
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(requestBody)
                    })
                """)
            ).await()
            val text = response.text().await()
            val result = json.decodeFromString<com.startspeler.dto.BulkCheckoutResponse>(text)
            if (response.ok && result.success) {
                setBulkModalOpen(false)
                setBulkSummary(null)
                fetchOrders(dateFrom, dateTo)
            } else {
                setBulkSummary(result.summary)
                setBulkError(result.error ?: "Bulk afrekenen mislukt")
            }
        } catch (e: Throwable) {
            setBulkError(e.message)
        } finally {
            setBulkLoading(false)
        }
    }

    suspend fun checkoutOrder(clientName: String) {
        setError(null)
        openBulkCheckoutSummaryForClient(clientName)
    }

    suspend fun deleteOrder(orderId: Int) {
        val url = backendUrl ?: return
        if (!canDeleteOrders) {
            setError("U heeft geen rechten om bestellingen te verwijderen")
            return
        }
        setError(null)
        try {
            val headers = Headers()
            headers.set("Authorization", "Bearer $jwtToken")
            val response = window.fetch(
                url.trimEnd('/') + "/order/$orderId",
                RequestInit(
                    method = "DELETE",
                    headers = headers
                )
            ).await()
            val text = response.text().await()

            val result = try {
                json.decodeFromString<DeleteOrderResponse>(text)
            } catch (_: Throwable) {
                DeleteOrderResponse(
                    success = response.ok && text.contains("success", ignoreCase = true),
                    deletedOrderId = if (response.ok) orderId else null,
                    error = if (response.ok) null else text.ifBlank { "Bestelling verwijderen mislukt" }
                )
            }

            if (response.ok && result.success) {
                setOrders { currentOrders -> currentOrders.filter { it.id != orderId } }
            } else {
                setError(result.error ?: "Bestelling verwijderen mislukt")
            }
        } catch (e: Throwable) {
            setError(e.message)
        }
    }

    useEffectOnce {
        window.fetch("/config.json")
            .then({ response -> response.json() })
            .then({ dataAny ->
                val data = dataAny.unsafeCast<dynamic>()
                setBackendUrl(data.backendUrl as? String)
            })
            .catch({ throwable ->
                setError("Error loading config.json: $throwable")
            })
    }

    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl != null) {
            MainScope().launch {
                fetchClients()
                fetchOrders(defaultFromTo.first, defaultFromTo.second)
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
        this.selectedDate = selectedDate
        this.onSelectedDateChange = handleSelectedDateChange
        this.onApplyDateFilter = { MainScope().launch { fetchOrders(dateFrom, dateTo) } }
        this.onCheckout = { clientName: String -> MainScope().launch { checkoutOrder(clientName) } }
        this.onDelete = { orderId: Int -> MainScope().launch { deleteOrder(orderId) } }
        this.canDeleteOrders = canDeleteOrders
        this.onMoveToNextStatus = { orderId: Int -> MainScope().launch { moveOrderStatus(orderId, "next") } }
        this.onMoveToPreviousStatus = { orderId: Int -> MainScope().launch { moveOrderStatus(orderId, "previous") } }
        this.clientOptions = clientOptions
        this.selectedClient = selectedClient
        this.clientInputError = clientInputError
        this.onSelectedClientChange = { clientName: String ->
            setSelectedClient(clientName)
            setClientInputError(null)
        }
        this.onOpenBulkCheckout = { MainScope().launch { openBulkCheckoutSummary() } }
        this.bulkSummary = bulkSummary
        this.bulkModalOpen = bulkModalOpen
        this.onBulkModalClose = {
            setBulkModalOpen(false)
            setBulkError(null)
        }
        this.onConfirmBulkCheckout = { fixedDiscountAmount: Float? -> MainScope().launch { confirmBulkCheckout(fixedDiscountAmount) } }
        this.bulkLoading = bulkLoading
        this.bulkError = bulkError
    }
}