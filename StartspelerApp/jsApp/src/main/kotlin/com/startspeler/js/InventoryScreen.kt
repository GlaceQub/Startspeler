package com.startspeler.js

import com.startspeler.ui.InventoryPage
import com.startspeler.ui.InventoryView
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

private val scope = MainScope()
private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class InventoryDto(
    val id: Int,
    val productId: Int,
    val quantity: Int,
    val minimumQuantity: Int? = null,
    val lastUpdated: String? = null
)

@Serializable
data class ProductMinDto(
    val id: Int,
    val name: String? = null
)

@Serializable
data class InventoryUpdateRequest(
    val quantity: Int,
    val minimumQuantity: Int? = null
)

external interface InventoryScreenProps : Props

val InventoryScreen = FC<InventoryScreenProps> {
    val (inventoryItems, setInventoryItems) = useState<List<InventoryDto>>(emptyList())
    val (products, setProducts) = useState<List<ProductMinDto>>(emptyList())
    val (merged, setMerged) = useState<List<dynamic>>(emptyList())
    val (selectedItem, setSelectedItem) = useState<InventoryView?>(null)
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val (loading, setLoading) = useState(false)
    val (error, setError) = useState<String?>(null)
    val (snackbarOpen, setSnackbarOpen) = useState(false)
    val (snackbarMsg, setSnackbarMsg) = useState("")

    useEffectOnce {
        window.fetch("/config.json")
            .then { resp ->
                if (!resp.ok) {
                    setError("Failed to load config.json: ${resp.status} ${resp.statusText}")
                    return@then
                }
                resp.json().then { d ->
                    val data = d.unsafeCast<dynamic>()
                    val url = data.backendUrl as? String
                    console.log("Loaded backendUrl:", url)
                    setBackendUrl(url)
                }
            }
            .catch { e -> setError("Error loading config.json: $e") }
    }

    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl == null) return@useEffect
        setLoading(true)
        scope.launch {
            try {
                val invResp = window.fetch(backendUrl.trimEnd('/') + "/inventory").await()
                val invText = invResp.text().await()
                console.log("GET /inventory status:", invResp.status, invResp.statusText)
                if (!invResp.ok) {
                    setError("Failed to fetch inventory: ${invResp.status} ${invResp.statusText}")
                    setInventoryItems(emptyList())
                } else {
                    val invList = json.decodeFromString(ListSerializer(InventoryDto.serializer()), invText)
                    setInventoryItems(invList)
                }

                val prodResp = window.fetch(backendUrl.trimEnd('/') + "/products").await()
                val prodText = prodResp.text().await()
                console.log("GET /products status:", prodResp.status, prodResp.statusText)
                if (prodResp.ok) {
                    val prodList = json.decodeFromString(ListSerializer(ProductMinDto.serializer()), prodText)
                    setProducts(prodList)
                } else {
                    setProducts(emptyList())
                }
            } catch (e: Throwable) {
                console.error("Fout bij het ophalen van inventory:", e)
                setError("Fout bij het ophalen van inventory: $e")
            } finally {
                setLoading(false)
            }
        }
    }

    useEffect(dependencies = arrayOf(inventoryItems, products)) {
        val prodMap = products.associateBy { it.id }
        val viewList = inventoryItems.map { inv ->
            val obj: dynamic = js("{}")
            obj.id = inv.id
            obj.productId = inv.productId
            obj.productName = prodMap[inv.productId]?.name ?: "Product #${inv.productId}"
            obj.quantity = inv.quantity
            obj.minimumQuantity = inv.minimumQuantity
            obj.lastUpdated = inv.lastUpdated
            obj
        }
        setMerged(viewList)
    }

    val handleEdit: (InventoryView) -> Unit = { item ->
        if (backendUrl == null) {
            setSnackbarMsg("Backend URL niet geladen.")
            setSnackbarOpen(true)
        } else {
            scope.launch {
                try {
                    val getResp = window.fetch(backendUrl.trimEnd('/') + "/inventory/${item.id}").await()
                    val getText = getResp.text().await()
                    console.log("GET /inventory/${item.id} status:", getResp.status, getResp.statusText)
                    if (getResp.ok) {
                        val current = json.decodeFromString(InventoryDto.serializer(), getText)
                        val inputQty = window.prompt(
                            "Nieuwe voorraad voor ${item.productName ?: "Product #${item.productId}"}",
                            current.quantity.toString()
                        )
                        val newQty = inputQty?.toIntOrNull()
                        if (inputQty == null) {
                            // geannuleerd
                        } else if (newQty == null || newQty < 0) {
                            setSnackbarMsg("Voer een geldige voorraad (>= 0) in.")
                            setSnackbarOpen(true)
                        } else {
                            val inputMin = window.prompt(
                                "Nieuwe minimale voorraad (leeg laten voor ongewijzigd)",
                                current.minimumQuantity?.toString() ?: ""
                            )
                            val newMin = inputMin?.ifBlank { null }?.toIntOrNull() ?: current.minimumQuantity
                            val req = InventoryUpdateRequest(quantity = newQty, minimumQuantity = newMin)
                            val bodyStr = json.encodeToString(req)
                            val init: dynamic = js("{}")
                            init.method = "PUT"
                            init.headers = js("{ 'Content-Type': 'application/json' }")
                            init.body = bodyStr
                            val putResp = window.fetch(backendUrl.trimEnd('/') + "/inventory/${item.id}", init).await()
                            val putText = putResp.text().await()
                            if (putResp.ok) {
                                val updated = json.decodeFromString(InventoryDto.serializer(), putText)
                                setInventoryItems(inventoryItems.map { if (it.id == updated.id) updated else it })
                                setSelectedItem(item)
                                setSnackbarMsg("Voorraad bijgewerkt")
                                setSnackbarOpen(true)
                            } else {
                                setSnackbarMsg("Bijwerken mislukt: ${putResp.status} ${putResp.statusText}")
                                setSnackbarOpen(true)
                            }
                        }
                    } else {
                        setSnackbarMsg("Item ophalen mislukt: ${getResp.status} ${getResp.statusText}")
                        setSnackbarOpen(true)
                    }
                } catch (e: Throwable) {
                    console.error("Fout bij bewerken:", e)
                    setSnackbarMsg("Fout bij bewerken: $e")
                    setSnackbarOpen(true)
                }
            }
        }
    }

    val handleDelete: (InventoryView) -> Unit = { item ->
        if (backendUrl == null) {
            setSnackbarMsg("Backend URL niet geladen.")
            setSnackbarOpen(true)
        } else {
            val confirmed = window.confirm("Weet je zeker dat je inventory item #${item.id} wilt verwijderen?")
            if (confirmed) {
                scope.launch {
                    try {
                        val url = backendUrl.trimEnd('/') + "/inventory/${item.id}"
                        val init: dynamic = js("{}")
                        init.method = "DELETE"
                        val resp = window.fetch(url, init).await()
                        val respText = resp.text().await()
                        console.log("DELETE $url status:", resp.status, resp.statusText)
                        console.log("DELETE body:", respText)
                        if (resp.ok) {
                            setInventoryItems(inventoryItems.filter { it.id != item.id })
                            setSelectedItem { null }
                            setSnackbarMsg("Inventory item verwijderd")
                            setSnackbarOpen(true)
                        } else {
                            setSnackbarMsg("Verwijderen mislukt: ${resp.status} ${resp.statusText}")
                            setSnackbarOpen(true)
                        }
                    } catch (e: Throwable) {
                        console.error("Fout bij verwijderen:", e)
                        setSnackbarMsg("Fout bij verwijderen: $e")
                        setSnackbarOpen(true)
                    }
                }
            }
        }
    }

    val handleSelect: (InventoryView?) -> Unit = { item ->
        setSelectedItem(item)
    }

    InventoryPage {
        this.items = merged.unsafeCast<List<InventoryView>>()
        this.loading = loading
        this.error = error
        this.onEdit = handleEdit
        this.onDelete = handleDelete
        this.onSelect = handleSelect
        this.selectedItem = selectedItem
    }

    Snackbar {
        open = snackbarOpen
        autoHideDuration = 3000
        onClose = { _, _ -> setSnackbarOpen(false) }
        Alert {
            severity = "info"
            sx = js("{ width: '100%' }")
            +snackbarMsg
        }
    }
}