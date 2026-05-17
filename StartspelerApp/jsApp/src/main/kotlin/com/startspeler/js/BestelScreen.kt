package com.startspeler.js

import com.startspeler.components.account.AddKlantModal
import com.startspeler.dto.CartItem
import com.startspeler.dto.InventoryDto
import com.startspeler.ui.BestelPage
import react.FC
import react.Props
import com.startspeler.dto.ProductItem
import com.startspeler.models.Category
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import react.useEffect
import react.useEffectOnce
import react.useState
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mui.material.Snackbar
import mui.material.Alert
import mui.material.Button
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.Typography

@Serializable
private data class InsufficientStockResponse(
    val error: String? = null,
    val products: List<String> = emptyList()
)

private fun mergeProductsWithCartStock(loadedProducts: List<ProductItem>, cartSnapshot: List<CartItem>): List<ProductItem> =
    loadedProducts.map { p ->
        val cartQty = cartSnapshot.find { it.product.id == p.id }?.quantity ?: 0
        val isBlockedByCartQuantity = p.stockQuantity > 0 && cartQty >= p.stockQuantity
        p.copy(outOfStock = p.outOfStock || isBlockedByCartQuantity)
    }

private data class CartReconcileResult(
    val adjustedCart: List<CartItem>,
    val changedProductNames: List<String>
)

external interface BestelScreenProps : Props {
    var initialTafelId: Int?
}

val BestelScreen = FC<BestelScreenProps> { props ->
    val json = Json { ignoreUnknownKeys = true }
    val (categories, setCategories) = useState<List<Category>>(emptyList())
    val (products, setProducts) = useState<List<ProductItem>>(emptyList())
    val (selectedCategory, setSelectedCategory) = useState<Category?>(null)
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val (error, setError) = useState<String?>(null)
    val (loadingCategories, setLoadingCategories) = useState(true)
    val (loadingProducts, setLoadingProducts) = useState(false)

    // Cart state
    val (cartItems, setCartItems) = useState<List<CartItem>>(emptyList())
    val (orderSnackbarOpen, setOrderSnackbarOpen) = useState(false)
    val (placingOrder, setPlacingOrder) = useState(false)
    val (stockConflictOpen, setStockConflictOpen) = useState(false)
    val (stockConflictProducts, setStockConflictProducts) = useState<List<String>>(emptyList())

    // Tafel and Klant state and logic
    val (tafelOptions, setTafelOptions) = useState<List<String>>(emptyList())
    val (selectedTafel, setSelectedTafel) = useState("")
    val klantOptions = useState<List<String>>(emptyList())
    val (klanten, setKlanten) = klantOptions
    val (selectedKlant, setSelectedKlant) = useState("")
    val (addKlantModalOpen, setAddKlantModalOpen) = useState(false)

    val handleTafelChange: (String) -> Unit = { setSelectedTafel(it) }
    val handleKlantChange: (String) -> Unit = { setSelectedKlant(it) }
    val handleAddKlant: () -> Unit = {
        setAddKlantModalOpen(true)
    }

    suspend fun refreshKlanten(selectName: String? = null) {
        val base = backendUrl ?: return

        val url = base.trimEnd('/') + "/klanten/names"
        val resp = window.fetch(url).await()
        val responseText = resp.text().await()

        val klantenList = json.decodeFromString<List<String>>(responseText)
        setKlanten(klantenList)

        when {
            selectName != null && klantenList.contains(selectName) -> setSelectedKlant(selectName)
            klantenList.isNotEmpty() -> setSelectedKlant(klantenList.first())
            else -> setSelectedKlant("")
        }
    }

    val handleKlantModalAdd: (String, String?) -> Unit = { name, email ->
        if (name.isNotBlank() && backendUrl != null) {
            MainScope().launch {
                try {
                    val url = backendUrl.trimEnd('/') + "/klant/add"

                    val bodyObj: dynamic = js("({})")
                    bodyObj.name = name
                    bodyObj.email = email
                    val requestInit = js("{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyObj) }")

                    val response = window.fetch(url, requestInit).await()
                    val responseText = response.text().await()

                    if (response.ok) {
                        refreshKlanten(selectName = name)
                    } else {
                        setError("Klant toevoegen mislukt: ${response.status} ${response.statusText} $responseText")
                    }
                } catch (e: Throwable) {
                    setError("Klant toevoegen mislukt: $e")
                } finally {
                    setAddKlantModalOpen(false)
                }
            }
        } else {
            setAddKlantModalOpen(false)
        }
    }
    val handleKlantModalClose: () -> Unit = {
        setAddKlantModalOpen(false)
    }

    // Load backendUrl from config.json on mount
    useEffectOnce {
        window.fetch("/config.json")
            .then { response ->
                if (!response.ok) {
                    setError("Failed to load config.json: ${'$'}{response.status} ${'$'}{response.statusText}")
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

    // Fetch categories when backendUrl is loaded
    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl != null) {
            setLoadingCategories(true)
            MainScope().launch {
                try {
                    val response = window.fetch(backendUrl.trimEnd('/') + "/categories")
                        .await()
                        .text()
                        .await()
                    val categories = json.decodeFromString<List<Category>>(response)
                    setCategories(categories)
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de categoriën: $e")
                } finally {
                    setLoadingCategories(false)
                }
            }
        }
    }

    val initialTafelId = props.initialTafelId

    suspend fun loadProductsForCategory(category: Category, cartSnapshot: List<CartItem> = cartItems) {
        val base = backendUrl ?: return
        setLoadingProducts(true)
        try {
            val response = window.fetch(base.trimEnd('/') + "/products/category/${category.id}/with-stock")
                .await()
                .text()
                .await()
            val loadedProducts = json.decodeFromString<List<ProductItem>>(response)
            setProducts(mergeProductsWithCartStock(loadedProducts, cartSnapshot))
        } catch (e: Throwable) {
            setError("Fout bij het ophalen van de producten: $e")
        } finally {
            setLoadingProducts(false)
        }
    }

    suspend fun refreshVisibleStock(cartSnapshot: List<CartItem> = cartItems) {
        val category = selectedCategory ?: return
        loadProductsForCategory(category, cartSnapshot)
    }

    fun refreshVisibleStockAsync(cartSnapshot: List<CartItem> = cartItems) {
        MainScope().launch {
            refreshVisibleStock(cartSnapshot)
        }
    }

    suspend fun fetchInventoryByProductId(): Map<Int, InventoryDto> {
        val base = backendUrl ?: return emptyMap()
        val responseText = window.fetch(base.trimEnd('/') + "/inventory")
            .await()
            .text()
            .await()
        val inventoryItems = json.decodeFromString(ListSerializer(InventoryDto.serializer()), responseText)
        return inventoryItems.associateBy { it.productId }
    }

    fun reconcileCart(cartSnapshot: List<CartItem>, inventoryByProductId: Map<Int, InventoryDto>): CartReconcileResult {
        val changedNames = mutableListOf<String>()
        val adjustedCart = cartSnapshot.mapNotNull { item ->
            val latestQuantity = inventoryByProductId[item.product.id]?.quantity ?: 0
            when {
                latestQuantity <= 0 -> {
                    changedNames += item.product.name
                    null
                }
                item.quantity > latestQuantity -> {
                    changedNames += item.product.name
                    item.copy(
                        product = item.product.copy(stockQuantity = latestQuantity, outOfStock = latestQuantity <= 0),
                        quantity = latestQuantity
                    )
                }
                else -> item.copy(
                    product = item.product.copy(stockQuantity = latestQuantity, outOfStock = latestQuantity <= 0)
                )
            }
        }
        return CartReconcileResult(adjustedCart = adjustedCart, changedProductNames = changedNames.distinct())
    }

    fun applyCartReconcileResult(result: CartReconcileResult) {
        setCartItems(result.adjustedCart)
        setStockConflictProducts(result.changedProductNames)
        setStockConflictOpen(result.changedProductNames.isNotEmpty())
        refreshVisibleStockAsync(result.adjustedCart)
    }

    // Fetch klanten and tafels from backend
    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl != null) {
            // Fetch klanten
            MainScope().launch {
                try {
                    refreshKlanten()
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de klanten: $e")
                }
            }

            // Fetch tafels
            MainScope().launch {
                try {
                    val response = window.fetch(backendUrl.trimEnd('/') + "/tafels").await().text().await()
                    val tafelList = json.decodeFromString<List<String>>(response)
                    setTafelOptions(tafelList)
                    // Auto-select from QR param if present, otherwise first
                    val matched = if (initialTafelId != null) tafelList.find { it.contains(initialTafelId.toString()) } else null
                    setSelectedTafel(matched ?: tafelList.firstOrNull() ?: "")
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de tafels: $e")
                }
            }
        }
    }

    val handleCategoryClick: (Category) -> Unit = { category ->
        setSelectedCategory(category)
        if (backendUrl != null) {
            MainScope().launch {
                loadProductsForCategory(category, cartItems)
            }
        }
    }

    val handleAddToCart: (ProductItem) -> Unit = { product ->
        if (!product.outOfStock) {
            val existing = cartItems.find { it.product.id == product.id }
            val newQuantity = (existing?.quantity ?: 0) + 1

            val newCart = if (existing != null) {
                cartItems.map { if (it.product.id == product.id) it.copy(quantity = newQuantity) else it }
            } else {
                cartItems + CartItem(product, 1)
            }
            setCartItems(newCart)
            setStockConflictProducts(stockConflictProducts.filter { conflictName -> newCart.any { it.product.name == conflictName } })

            // Zet outOfStock lokaal op true als cart quantity de beschikbare stock bereikt
            setProducts(products.map { p ->
                if (p.id == product.id && newQuantity >= p.stockQuantity) {
                    p.copy(outOfStock = true)
                } else p
            })
        }
    }
    val handleRemoveFromCart: (CartItem) -> Unit = { item ->
        val existing = cartItems.find { it.product.id == item.product.id }
        val newQuantity = if (existing != null && existing.quantity > 1) existing.quantity - 1 else 0
        val updatedCart = if (newQuantity > 0) {
            cartItems.map { if (it.product.id == item.product.id) it.copy(quantity = newQuantity) else it }
        } else {
            cartItems.filterNot { it.product.id == item.product.id }
        }
        if (newQuantity > 0) {
            setCartItems(updatedCart)
        } else {
            setCartItems(updatedCart)
        }
        val remainingConflictProducts = stockConflictProducts.filter { conflictName ->
            updatedCart.any { it.product.name == conflictName }
        }
        setStockConflictProducts(remainingConflictProducts)
        if (remainingConflictProducts.isEmpty()) {
            setStockConflictOpen(false)
        }
        // Zet outOfStock terug op false als er minder in de cart zit dan de stock
        setProducts(products.map { p ->
            if (p.id == item.product.id && newQuantity < p.stockQuantity) {
                p.copy(outOfStock = false)
            } else p
        })
    }
    val handleOrder: () -> Unit = {
        if (backendUrl == null){
            setError("Backend URL niet geladen")
        } else if (cartItems.isEmpty()) {
            setError("Geen producten in de bestelling")
        } else {
            val orderData = js("{}")
            orderData.klant = selectedKlant
            orderData.tafel = selectedTafel
            orderData.items = cartItems.map { cartItem ->
                val obj: dynamic = js("({})")
                obj.productId = cartItem.product.id
                obj.quantity = cartItem.quantity
                obj.price = cartItem.product.price
                obj
            }.toTypedArray()
            MainScope().launch {
                setPlacingOrder(true)
                try {
                    val inventoryByProductId = fetchInventoryByProductId()
                    val preflightResult = reconcileCart(cartItems, inventoryByProductId)
                    if (preflightResult.changedProductNames.isNotEmpty()) {
                        setPlacingOrder(false)
                        applyCartReconcileResult(preflightResult)
                        return@launch
                    }

                    val requestInit = js("{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(orderData) }")
                    val response = withTimeout(15000) {
                        window.fetch(backendUrl.trimEnd('/') + "/order/add", requestInit).await()
                    }
                    val responseText = withTimeout(5000) {
                        response.text().await()
                    }
                    setPlacingOrder(false)
                    if (response.ok) {
                        setCartItems(emptyList())
                        setStockConflictProducts(emptyList())
                        setStockConflictOpen(false)
                        setOrderSnackbarOpen(true)
                        refreshVisibleStockAsync(emptyList())
                    } else if (response.asDynamic().status == 409) {
                        val conflict = try {
                            json.decodeFromString<InsufficientStockResponse>(responseText)
                        } catch (_: Throwable) {
                            InsufficientStockResponse(products = emptyList())
                        }
                        val latestInventory = fetchInventoryByProductId()
                        val reconciled = reconcileCart(cartItems, latestInventory)
                        if (reconciled.changedProductNames.isNotEmpty()) {
                            applyCartReconcileResult(reconciled)
                        } else {
                            setStockConflictProducts(conflict.products)
                            setStockConflictOpen(true)
                            refreshVisibleStockAsync(cartItems)
                        }
                    } else {
                        setError("Bestelling plaatsen mislukt: " + response.status + " " + response.statusText + if (responseText.isNotBlank()) " $responseText" else "")
                    }
                } catch (_: TimeoutCancellationException) {
                    setError("De server reageert niet op tijd bij het plaatsen van de bestelling. Probeer opnieuw.")
                } catch (e: Throwable) {
                    setError("Bestelling plaatsen mislukt: $e")
                } finally {
                    setPlacingOrder(false)
                }
            }
        }
    }

    // Always render BestelPage, pass error to Menu
    BestelPage {
        this.categories = categories
        this.products = products
        this.selectedCategory = selectedCategory
        this.onCategoryClick = handleCategoryClick
        this.onBackClick = {
            setSelectedCategory(null)
            setProducts(emptyList())
        }
        this.loading = loadingCategories || loadingProducts
        this.error = error
        this.cartItems = cartItems
        this.onAddToCart = handleAddToCart
        this.onRemoveFromCart = handleRemoveFromCart
        this.OnOrder = handleOrder
        this.tafelOptions = tafelOptions
        this.selectedTafel = selectedTafel
        this.onTafelChange = handleTafelChange
        this.klantOptions = klanten
        this.selectedKlant = selectedKlant
        this.onKlantChange = handleKlantChange
        this.onAddKlant = handleAddKlant
        this.submitLabel = if (placingOrder) "Bestelling controleren..." else "Bestellen"
        this.isSubmitting = placingOrder
        this.conflictingProductNames = stockConflictProducts
    }

    AddKlantModal {
        open = addKlantModalOpen
        onClose = handleKlantModalClose
        onAdd = handleKlantModalAdd
        existingNames = klanten
        existingEmails = emptyList()
    }

    Dialog {
        open = stockConflictOpen
        onClose = { _, _ -> setStockConflictOpen(false) }

        DialogTitle {
            +"Niet alles is nog in voorraad"
        }
        DialogContent {
            Typography {
                +"Je winkelwagen is aangepast aan de actuele voorraad." 
            }
            if (stockConflictProducts.isNotEmpty()) {
                Typography {
                    sx = js("{ marginTop: '16px', fontWeight: 700 }")
                    +"Aangepast:"
                }
                stockConflictProducts.forEach { productName ->
                    Typography {
                        sx = js("{ marginTop: '8px' }")
                        +"• $productName"
                    }
                }
            }
        }
        DialogActions {
            Button {
                onClick = { setStockConflictOpen(false) }
                +"Bestelling aanpassen"
            }
        }
    }

    // Snackbar for order placed
    Snackbar {
        open = orderSnackbarOpen
        autoHideDuration = 3000
        onClose = { _, _ -> setOrderSnackbarOpen(false) }
        Alert {
            severity = "success"
            sx = js("{ width: '100%' }")
            +"Bestelling geplaatst!"
        }
    }
}