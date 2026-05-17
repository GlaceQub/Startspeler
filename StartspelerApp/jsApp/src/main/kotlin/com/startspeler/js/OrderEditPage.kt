package com.startspeler.js

import com.startspeler.dto.CartItem
import com.startspeler.dto.ProductItem
import com.startspeler.models.Category
import com.startspeler.ui.BestelPage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import mui.material.Box
import mui.material.CircularProgress
import mui.material.Typography
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState

external interface OrderEditPageProps : Props {
    var backendUrl: String?
    var onSaved: ((Int) -> Unit)?
}

private val json = Json { ignoreUnknownKeys = true }

val OrderEditPage = FC<OrderEditPageProps> { props ->
    val raw = window.location.hash.removePrefix("#/")
    val prefix = "bestel/edit/"
    val orderIdStr = if (raw.startsWith(prefix)) raw.removePrefix(prefix) else null
    if (orderIdStr == null || orderIdStr.isBlank()) {
        div { +"Geen bestelling ID opgegeven" }
        return@FC
    }

    val orderId = orderIdStr.toIntOrNull()
    if (orderId == null) {
        div { +"Ongeldig bestelling ID" }
        return@FC
    }

    val (backend, setBackend) = useState<String?>(props.backendUrl)
    val (loading, setLoading) = useState(true)
    val (saving, setSaving) = useState(false)
    val (loadingProducts, setLoadingProducts) = useState(false)
    val (error, setError) = useState<String?>(null)
    val (categories, setCategories) = useState<List<Category>>(emptyList())
    val (products, setProducts) = useState<List<ProductItem>>(emptyList())
    val (selectedCategory, setSelectedCategory) = useState<Category?>(null)
    val (cartItems, setCartItems) = useState<List<CartItem>>(emptyList())
    val (tafelOptions, setTafelOptions) = useState<List<String>>(emptyList())
    val (selectedTafel, setSelectedTafel) = useState("")
    val (klanten, setKlanten) = useState<List<String>>(emptyList())
    val (selectedKlant, setSelectedKlant) = useState("")

    // Load backend from config.json if not provided
    useEffect(dependencies = arrayOf(props.backendUrl)) {
        if (props.backendUrl != null) {
            setBackend(props.backendUrl)
        } else {
            MainScope().launch {
                try {
                    val cfgRes = window.fetch("/config.json").await()
                    if (cfgRes.ok) {
                        val cfgText = cfgRes.text().await()
                        val cfg = JSON.parse<dynamic>(cfgText)
                        setBackend(cfg?.backendUrl as? String)
                    } else {
                        setError("Kon config.json niet laden: ${'$'}{cfgRes.status}")
                    }
                } catch (e: Throwable) {
                    setError("Kon config.json niet laden: $e")
                }
            }
        }
    }


    // Fetch order and related data when backend is available
    useEffect(dependencies = arrayOf(backend)) {
        val b = backend ?: return@useEffect
        setLoading(true)
        MainScope().launch {
            try {
                // Fetch order details
                val orderRes = window.fetch(b.trimEnd('/') + "/order/$orderId").await()
                if (!orderRes.ok) {
                    setError("Kon bestelling niet laden: ${'$'}{orderRes.status} ${'$'}{orderRes.statusText}")
                    setLoading(false)
                    return@launch
                }

                val orderText = orderRes.text().await()
                val orderDto = json.decodeFromString<com.startspeler.dto.OrderOverzichtItem>(orderText)

                // Fetch categories, products, klanten, and tafels
                try {
                    val catRes = window.fetch(b.trimEnd('/') + "/categories").await()
                    if (catRes.ok) {
                        val catText = catRes.text().await()
                        setCategories(json.decodeFromString<List<Category>>(catText))
                    }
                } catch (_: Throwable) {}

                try {
                    val klantenRes = window.fetch(b.trimEnd('/') + "/klanten/names").await()
                    if (klantenRes.ok) {
                        val klantenText = klantenRes.text().await()
                        val klantenList = json.decodeFromString<List<String>>(klantenText)
                        setKlanten(klantenList)
                        setSelectedKlant(orderDto.clientName.ifBlank { klantenList.firstOrNull() ?: "" })
                    }
                } catch (_: Throwable) {
                    setSelectedKlant(orderDto.clientName)
                }

                try {
                    val tafelsRes = window.fetch(b.trimEnd('/') + "/tafels").await()
                    if (tafelsRes.ok) {
                        val tafelsText = tafelsRes.text().await()
                        val tafelList = json.decodeFromString<List<String>>(tafelsText)
                        setTafelOptions(tafelList)
                        val desiredTafel = orderDto.tableNumber.toString()
                        setSelectedTafel(if (tafelList.contains(desiredTafel)) desiredTafel else tafelList.firstOrNull() ?: desiredTafel)
                    }
                } catch (_: Throwable) {
                    setSelectedTafel(orderDto.tableNumber.toString())
                }

                // Store current order as cart using lightweight product placeholders; real menu products are loaded per category.
                val mapped = orderDto.orderitems.map { oi ->
                    CartItem(
                        ProductItem(
                            id = oi.productId,
                            name = oi.product,
                            price = oi.price,
                            outOfStock = false,
                            stockQuantity = 0
                        ),
                        oi.quantity
                    )
                }
                setCartItems(mapped)
            } catch (e: Throwable) {
                setError("Kon bestelling niet laden: $e")
            } finally {
                setLoading(false)
            }
        }
    }

    // Update product stock flags based on cart contents
    fun updateProductStockFlags(nextCart: List<CartItem>) {
        setProducts { currentProducts ->
            currentProducts.map { product ->
                val inCart = nextCart.find { it.product.id == product.id }?.quantity ?: 0
                if (product.stockQuantity > 0) {
                    product.copy(outOfStock = inCart >= product.stockQuantity)
                } else product
            }
        }
    }

    // Handle category selection: load products for the selected category
    val handleCategoryClick: (Category) -> Unit = { category ->
        setSelectedCategory(category)
        val b = backend
        if (b != null) {
            setLoadingProducts(true)
            MainScope().launch {
                try {
                    val response = window.fetch(b.trimEnd('/') + "/products/category/${category.id}/with-stock")
                        .await()
                        .text()
                        .await()
                    val loadedProducts = json.decodeFromString<List<ProductItem>>(response)
                    val updatedProducts = loadedProducts.map { p ->
                        val cartQty = cartItems.find { it.product.id == p.id }?.quantity ?: 0
                        if (cartQty >= p.stockQuantity && p.stockQuantity > 0) p.copy(outOfStock = true) else p
                    }
                    setProducts(updatedProducts)
                } catch (e: Throwable) {
                    setError("Fout bij het ophalen van de producten: $e")
                } finally {
                    setLoadingProducts(false)
                }
            }
        }
    }

    if (loading) {
        Box { CircularProgress {} }
        return@FC
    }
    if (error != null) {
        div { +error }
        return@FC
    }

    // Define onOrderSubmit: will be called by BestelPage with current cart and metadata
    val onOrderSubmit: (List<CartItem>, String, String) -> Unit = { currentCart, tafel, klant ->
        val b = backend
        if (b == null) {
            window.alert("Backend URL niet beschikbaar")
        } else if (currentCart.isEmpty()) {
            window.alert("De bestelling moet minstens één item bevatten")
        } else {
            setSaving(true)
            MainScope().launch {
                try {
                    val orderData = js("{}")
                    orderData.klant = klant
                    orderData.tafel = tafel
                    orderData.items = currentCart.map { ci ->
                        val o = js("{}")
                        o.productId = ci.product.id
                        o.quantity = ci.quantity
                        o.price = ci.product.price
                        o
                    }.toTypedArray()

                    val requestInit = js("{}")
                    requestInit.method = "PUT"
                    requestInit.headers = js("{}")
                    requestInit.headers["Content-Type"] = "application/json"
                    requestInit.body = JSON.stringify(orderData)

                    val res = window.fetch(b.trimEnd('/') + "/order/$orderId", requestInit).await()
                    if (res.ok) {
                        props.onSaved?.invoke(orderId)
                        window.location.hash = "#/bestellingen"
                    } else if (res.status.toInt() == 409) {
                        val txt = res.text().await()
                        console.error("Order update conflict:", txt)
                        window.alert("Niet alle producten zijn nog in voorraad. De pagina wordt vernieuwd.")
                        window.location.reload()
                    } else {
                        val txt = res.text().await()
                        console.error("Order update failed response text:", txt)
                        window.alert("Update mislukt: ${'$'}{res.status} ${'$'}txt")
                    }
                } catch (e: Throwable) {
                    console.error("Order update failed:", e)
                    window.alert("Update mislukt: ${'$'}e")
                } finally {
                    setSaving(false)
                }
            }
        }
    }

    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', width: '100%', padding: '24px', boxSizing: 'border-box' }")
        Typography {
            variant = mui.material.styles.TypographyVariant.h5
            sx = js("{ fontWeight: 700, marginBottom: '16px' }")
            +"Bestelling aanpassen #$orderId"
        }
        Typography {
            variant = mui.material.styles.TypographyVariant.body2
            sx = js("{ color: '#5f6368', marginBottom: '16px' }")
            +"Pas de bestelling aan en klik op Opslaan om terug te keren naar het bestellingenoverzicht."
        }
        BestelPage {
            this.products = products
            this.categories = categories
            this.onCategoryClick = handleCategoryClick
            this.selectedCategory = selectedCategory
            this.onBackClick = {
                setSelectedCategory(null)
                setProducts(emptyList())
            }
            this.loading = saving || loadingProducts
            this.error = error
            this.cartItems = cartItems
            this.onAddToCart = { product ->
                val existing = cartItems.find { it.product.id == product.id }
                val nextQuantity = (existing?.quantity ?: 0) + 1
                if (product.stockQuantity > 0 && nextQuantity > product.stockQuantity) {
                    window.alert("Er is niet voldoende voorraad voor ${'$'}{product.name}")
                } else {
                    val nextCart = if (existing != null) {
                        cartItems.map { if (it.product.id == product.id) it.copy(quantity = nextQuantity) else it }
                    } else {
                        cartItems + CartItem(product, 1)
                    }
                    setCartItems(nextCart)
                    updateProductStockFlags(nextCart)
                }
            }
            this.onRemoveFromCart = { cartItem ->
                val existing = cartItems.find { it.product.id == cartItem.product.id }
                val nextQuantity = if (existing != null) existing.quantity - 1 else 0
                val nextCart = if (nextQuantity > 0) {
                    cartItems.map { if (it.product.id == cartItem.product.id) it.copy(quantity = nextQuantity) else it }
                } else {
                    cartItems.filterNot { it.product.id == cartItem.product.id }
                }
                setCartItems(nextCart)
                updateProductStockFlags(nextCart)
            }
            this.OnOrder = { }
            this.tafelOptions = tafelOptions
            this.selectedTafel = selectedTafel
            this.onTafelChange = { setSelectedTafel(it) }
            this.klantOptions = klanten
            this.selectedKlant = selectedKlant
            this.onKlantChange = { setSelectedKlant(it) }
            this.onAddKlant = { window.location.hash = "#/usercreate" }
            this.onOrderSubmit = onOrderSubmit
            this.submitLabel = "Opslaan"
            this.isSubmitting = saving
            this.conflictingProductNames = emptyList()
        }
    }
}
