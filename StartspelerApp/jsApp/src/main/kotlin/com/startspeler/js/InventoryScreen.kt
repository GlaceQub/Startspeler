package com.startspeler.js

import com.startspeler.ui.InventoryPage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
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

external interface InventoryScreenProps : Props

val InventoryScreen = FC<InventoryScreenProps> {
    val (inventoryItems, setInventoryItems) = useState<List<InventoryDto>>(emptyList())
    val (products, setProducts) = useState<List<ProductMinDto>>(emptyList())
    val (merged, setMerged) = useState<List<dynamic>>(emptyList())
    val (selectedItem, setSelectedItem) = useState<dynamic>(null)
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val (loading, setLoading) = useState(false)
    val (error, setError) = useState<String?>(null)
    val (snackbarOpen, setSnackbarOpen) = useState(false)
    val (snackbarMsg, setSnackbarMsg) = useState("")

    // load backend url
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

    // fetch inventory and products
    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl == null) return@useEffect
        setLoading(true)
        scope.launch {
            try {
                val invResp = window.fetch(backendUrl.trimEnd('/') + "/inventory").await()
                val invText = invResp.text().await()
                console.log("GET /inventory status:", invResp.status, invResp.statusText)
                console.log("GET /inventory body:", invText)
                if (!invResp.ok) {
                    setError("Failed to fetch inventory: ${invResp.status} ${invResp.statusText}")
                    setInventoryItems(emptyList())
                } else {
                    val invList = json.decodeFromString(ListSerializer(InventoryDto.serializer()), invText)
                    console.log("Parsed inventory items:", invList.size)
                    setInventoryItems(invList)
                }

                val prodResp = window.fetch(backendUrl.trimEnd('/') + "/products").await()
                val prodText = prodResp.text().await()
                console.log("GET /products status:", prodResp.status, prodResp.statusText)
                console.log("GET /products body:", prodText)
                if (prodResp.ok) {
                    val prodList = json.decodeFromString(ListSerializer(ProductMinDto.serializer()), prodText)
                    console.log("Parsed products:", prodList.size)
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

    // merge inventory + product names into view objects (zonder jso, met dynamic)
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
        console.log("Merged view items:", viewList.size)
        setMerged(viewList)
    }

    // callbacks (placeholder behavior for now)
    val handleAdd: () -> Unit = {
        setSnackbarMsg("Add inventory - nog niet geïmplementeerd")
        setSnackbarOpen(true)
    }
    val handleEdit: (dynamic) -> Unit = { item ->
        setSnackbarMsg("Edit inventory id=${item.id} - nog niet geïmplementeerd")
        setSnackbarOpen(true)
    }
    val handleDelete: (dynamic) -> Unit = { item ->
        setSnackbarMsg("Delete inventory id=${item.id} - nog niet geïmplementeerd")
        setSnackbarOpen(true)
    }
    val handleSelect: (dynamic) -> Unit = { item ->
        setSelectedItem(item)
    }

    InventoryPage {
        this.items = merged.unsafeCast<List<com.startspeler.ui.InventoryView>>()
        this.loading = loading
        this.error = error
        this.onAdd = handleAdd
        this.onEdit = { it: com.startspeler.ui.InventoryView -> handleEdit(it) }
        this.onDelete = { it: com.startspeler.ui.InventoryView -> handleDelete(it) }
        this.onSelect = { it: com.startspeler.ui.InventoryView? -> handleSelect(it) }
        this.selectedItem = selectedItem.unsafeCast<com.startspeler.ui.InventoryView?>()
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