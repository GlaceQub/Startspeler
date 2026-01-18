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

    // Tafel and Klant state and logic
    val tafelOptions = listOf("Tafel 1", "Tafel 2", "Tafel 3") // Replace with API data if needed
    val (selectedTafel, setSelectedTafel) = useState(tafelOptions.first())
    val klantOptions = useState(listOf("Jan", "Piet", "Klaas")) // Replace with API data if needed
    val (klanten, setKlanten) = klantOptions
    val (selectedKlant, setSelectedKlant) = useState(klanten.first())
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
                    val products = Json.decodeFromString<List<ProductItem>>(response)
                    setProducts(products)
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de producten: ${'$'}e")
                } finally {
                    setLoadingProducts(false)
                }
            }
        }
    }

    val handleAddToCart: (ProductItem) -> Unit = { product ->
        val existing = cartItems.find { it.product.id == product.id }
        if (existing != null) {
            setCartItems(cartItems.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it })
        } else {
            setCartItems(cartItems + CartItem(product, 1))
        }
    }
    val handleRemoveFromCart: (CartItem) -> Unit = { item ->
        val existing = cartItems.find { it.product.id == item.product.id }
        if (existing != null && existing.quantity > 1) {
            setCartItems(cartItems.map { if (it.product.id == item.product.id) it.copy(quantity = it.quantity - 1) else it })
        } else {
            setCartItems(cartItems.filterNot { it.product.id == item.product.id })
        }
    }
    val handleOrder: () -> Unit = {
        // Send API request to place order here (not implemented)
        // After successful order:
        setCartItems(emptyList())
        setOrderSnackbarOpen(true)
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
}
