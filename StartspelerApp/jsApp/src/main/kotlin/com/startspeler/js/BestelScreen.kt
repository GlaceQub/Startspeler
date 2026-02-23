package com.startspeler.js

import com.startspeler.components.account.AddKlantModal
import com.startspeler.dto.CartItem
import com.startspeler.ui.BestelPage
import react.FC
import react.Props
import com.startspeler.dto.ProductItem
import com.startspeler.models.Category
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import react.useEffect
import react.useEffectOnce
import react.useState
import kotlinx.serialization.json.Json
import mui.material.Snackbar
import mui.material.Alert
import com.startspeler.dto.klantToevoegen

external interface BestelScreenProps : Props

val BestelScreen = FC<BestelScreenProps> {
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
    val (stockErrorSnackbarOpen, setStockErrorSnackbarOpen) = useState(false)

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
    val handleKlantModalAdd: (String, String?) -> Unit = { name, email ->
        if (name.isNotBlank() && backendUrl != null) {
            MainScope().launch {
                try {
                    val bodyObj = js("{ name: name, email: email }")
                    val requestInit = js("{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyObj) }")
                    val response = window.fetch(
                        backendUrl.trimEnd('/') + "/klant/add",
                        requestInit
                    ).await()
                    if (response.ok) {
                        setKlanten(klanten + name)
                        setSelectedKlant(name)
                    } else {
                        setError("Klant toevoegen mislukt: ${'$'}{response.status} ${'$'}{response.statusText}")
                    }
                } catch (_: Throwable) {
                    setError("Klant toevoegen mislukt")
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
                setError("Error loading config.json: ${'$'}throwable")
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
                    val categories = Json.decodeFromString<List<Category>>(response)
                    setCategories(categories)
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de categoriën: ${'$'}e")
                } finally {
                    setLoadingCategories(false)
                }
            }
        }
    }

    // Fetch klanten and tafels from backend
    useEffect(dependencies = arrayOf(backendUrl)) {
        if (backendUrl != null) {
            // Fetch klanten
            MainScope().launch {
                try {
                    val response = window.fetch(backendUrl.trimEnd('/') + "/klanten").await().text().await()
                    val klantenList = Json.decodeFromString<List<String>>(response)
                    setKlanten(klantenList)
                    setSelectedKlant("")
                    if (klantenList.isNotEmpty()) {
                        setSelectedKlant(klantenList.first())
                    }
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de klanten: ${'$'}e")
                }
            }
            // Fetch tafels
            MainScope().launch {
                try {
                    val response = window.fetch(backendUrl.trimEnd('/') + "/tafels").await().text().await()
                    val tafelList = Json.decodeFromString<List<String>>(response)
                    setTafelOptions(tafelList)
                    setSelectedTafel("")
                    if (tafelList.isNotEmpty()) {
                        setSelectedTafel(tafelList.first())
                    }
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de tafels: ${'$'}e")
                }
            }
        }
    }

    val handleCategoryClick: (Category) -> Unit = { category ->
        setSelectedCategory(category)
        if (backendUrl != null) {
            setLoadingProducts(true)
            MainScope().launch {
                try {
                    val response = window.fetch(backendUrl.trimEnd('/') + "/products/category/${category.id}/with-stock")
                        .await()
                        .text()
                        .await()
                    val loadedProducts = Json.decodeFromString<List<ProductItem>>(response)
                    // Pas outOfStock aan op basis van huidige cart
                    val updatedProducts = loadedProducts.map { p ->
                        val cartQty = cartItems.find { it.product.id == p.id }?.quantity ?: 0
                        if (cartQty >= p.stockQuantity && p.stockQuantity > 0) {
                            p.copy(outOfStock = true)
                        } else p
                    }
                    setProducts(updatedProducts)
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de producten: ${'$'}e")
                } finally {
                    setLoadingProducts(false)
                }
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
        if (newQuantity > 0) {
            setCartItems(cartItems.map { if (it.product.id == item.product.id) it.copy(quantity = newQuantity) else it })
        } else {
            setCartItems(cartItems.filterNot { it.product.id == item.product.id })
        }
        // Zet outOfStock terug op false als er minder in de cart zit dan de stock
        setProducts(products.map { p ->
            if (p.id == item.product.id && newQuantity < p.stockQuantity) {
                p.copy(outOfStock = false)
            } else p
        })
    }
    val handleOrder: () -> Unit = {
        console.log("Placing order with items:", cartItems)
        if (backendUrl == null) {
            setError("Backend URL niet geladen")
        } else if (cartItems.isEmpty()) {
            setError("Geen producten in de bestelling")
        } else {
            val orderData = js("{}")
            orderData.klant = selectedKlant
            orderData.tafel = selectedTafel
            orderData.items = cartItems.map { item ->
                js("{ productId: item.product_1.id_1, quantity: item.quantity_1, price: item.product_1.price_1 }")
            }.toTypedArray()
            MainScope().launch {
                try {
                    val requestInit = js("{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(orderData) }")
                    val response = window.fetch(backendUrl.trimEnd('/') + "/order/add", requestInit).await()
                    if (response.ok) {
                        setCartItems(emptyList())
                        setOrderSnackbarOpen(true)
                    } else if (response.asDynamic().status == 409) {
                        setStockErrorSnackbarOpen(true)
                        js("setTimeout(function() { window.location.reload(); }, 5000)")
                    } else {
                        setError("Bestelling plaatsen mislukt: "+response.status+" "+response.statusText)
                    }
                } catch (e: Throwable) {
                    setError("Bestelling plaatsen mislukt: ${'$'}e")
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
    }

    AddKlantModal {
        open = addKlantModalOpen
        onClose = handleKlantModalClose
        onAdd = handleKlantModalAdd
        existingNames = klanten
        existingEmails = emptyList()
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

    // Snackbar for insufficient stock
    Snackbar {
        open = stockErrorSnackbarOpen
        autoHideDuration = 5000
        onClose = { _, _ -> setStockErrorSnackbarOpen(false) }
        Alert {
            severity = "error"
            sx = js("{ width: '100%' }")
            +"Sorry, kon niet elk product toevoegen omdat het niet meer in voorraad is."
        }
    }
}
