package com.startspeler.js

import com.startspeler.dto.CategoryDto
import com.startspeler.dto.ProductCreateUpdateDto
import com.startspeler.dto.ProductDto
import com.startspeler.ui.ProductPage
import com.startspeler.ui.ProductView
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
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

external interface ProductScreenProps : Props

val ProductScreen = FC<ProductScreenProps> {
    val (products, setProducts) = useState<List<ProductDto>>(emptyList())
    val (categories, setCategories) = useState<List<CategoryDto>>(emptyList())
    val (merged, setMerged) = useState<List<dynamic>>(emptyList())
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
                val prodResp = window.fetch(backendUrl.trimEnd('/') + "/products").await()
                val prodText = prodResp.text().await()
                if (prodResp.ok) {
                    val prodList = json.decodeFromString(ListSerializer(ProductDto.serializer()), prodText)
                    setProducts(prodList)
                } else {
                    setError("Failed to fetch products: ${prodResp.status} ${prodResp.statusText}")
                    setProducts(emptyList())
                }

                val catResp = window.fetch(backendUrl.trimEnd('/') + "/categories").await()
                val catText = catResp.text().await()
                if (catResp.ok) {
                    val catList = json.decodeFromString(ListSerializer(CategoryDto.serializer()), catText)
                    setCategories(catList)
                } else {
                    setCategories(emptyList())
                }
            } catch (e: Throwable) {
                setError("Fout bij ophalen: $e")
            } finally {
                setLoading(false)
            }
        }
    }

    useEffect(dependencies = arrayOf(products, categories)) {
        val catMap = categories.associateBy { it.id }
        val viewList = products.map { p ->
            val obj: dynamic = js("{}")
            obj.id = p.id
            obj.name = p.name
            obj.categoryId = p.categoryId
            obj.categoryName = catMap[p.categoryId]?.name ?: "Categorie #${p.categoryId}"
            obj.price = p.price
            obj.popularity = p.popularity
            obj
        }
        setMerged(viewList)
    }

    val handleAdd: (String, Int, Float, Int) -> Unit = add@{ name, categoryId, price, popularity ->
        val url = backendUrl

        if (price < 0) {
            setSnackbarMsg("Prijs mag niet negatief zijn.")
            setSnackbarOpen(true)
            return@add
        }

        if (url == null) {
            setSnackbarMsg("Backend URL niet geladen.")
            setSnackbarOpen(true)
        } else {
            scope.launch {
                try {
                    val body = json.encodeToString(
                        com.startspeler.dto.ProductCreateUpdateDto(
                            name = name,
                            categoryId = categoryId,
                            price = price,
                            popularity = popularity
                        )
                    )
                    val init: dynamic = js("{}")
                    init.method = "POST"
                    init.headers = js("{ 'Content-Type': 'application/json' }")
                    init.body = body

                    val resp = window.fetch(url.trimEnd('/') + "/products", init).await()
                    val text = resp.text().await()
                    if (resp.ok) {
                        setSnackbarMsg("Product toegevoegd")
                        setSnackbarOpen(true)

                        val prodResp = window.fetch(url.trimEnd('/') + "/products").await()
                        val prodText = prodResp.text().await()
                        if (prodResp.ok) {
                            val prodList = json.decodeFromString(ListSerializer(ProductDto.serializer()), prodText)
                            setProducts(prodList)
                        }
                    } else {
                        setSnackbarMsg("Toevoegen mislukt: ${resp.status} ${resp.statusText} $text")
                        setSnackbarOpen(true)
                    }
                } catch (e: Throwable) {
                    setSnackbarMsg("Fout bij toevoegen: $e")
                    setSnackbarOpen(true)
                }
            }
                Unit
        }
    }

    val handleEdit: (Int, String, Int, Float, Int) -> Unit = edit@{ id, name, categoryId, price, popularity ->
        val url = backendUrl

        if (price < 0) {
            setSnackbarMsg("Prijs mag niet negatief zijn.")
            setSnackbarOpen(true)
                return@edit
        }

        if (url == null) {
            setSnackbarMsg("Backend URL niet geladen.")
            setSnackbarOpen(true)
        } else {
            scope.launch {
                try {
                    val body = json.encodeToString(
                        ProductCreateUpdateDto(
                            name = name,
                            categoryId = categoryId,
                            price = price,
                            popularity = popularity
                        )
                    )

                    val init: dynamic = js("{}")
                    init.method = "PUT"
                    init.headers = js("{ 'Content-Type': 'application/json' }")
                    init.body = body

                    val resp = window.fetch(url.trimEnd('/') + "/products/$id", init).await()
                    val text = resp.text().await()
                    if (resp.ok) {
                        setSnackbarMsg("Product bijgewerkt")
                        setSnackbarOpen(true)

                        val prodResp = window.fetch(url.trimEnd('/') + "/products").await()
                        val prodText = prodResp.text().await()
                        if (prodResp.ok) {
                            val prodList = json.decodeFromString(ListSerializer(ProductDto.serializer()), prodText)
                            setProducts(prodList)
                        }
                    } else {
                        setSnackbarMsg("Bijwerken mislukt: ${resp.status} ${resp.statusText} $text")
                        setSnackbarOpen(true)
                    }
                } catch (e: Throwable) {
                    setSnackbarMsg("Fout bij bewerken: $e")
                    setSnackbarOpen(true)
                }
            }
            Unit
        }
    }

    val handleDelete: (Int) -> Unit = { id ->
        val url = backendUrl
        if (url == null) {
            setSnackbarMsg("Backend URL niet geladen.")
            setSnackbarOpen(true)
        } else {
            val confirmed = window.confirm("Weet je zeker dat je product #$id wil verwijderen?")
            if (confirmed) {
                scope.launch {
                    try {
                        val init: dynamic = js("{}")
                        init.method = "DELETE"
                        val resp = window.fetch(url.trimEnd('/') + "/products/$id", init).await()
                        val text = resp.text().await()
                        if (resp.ok) {
                            setProducts(products.filter { it.id != id })
                            setSnackbarMsg("Product verwijderd")
                        } else {
                            setSnackbarMsg("Verwijderen mislukt: ${resp.status} ${resp.statusText} $text")
                        }
                        setSnackbarOpen(true)
                    } catch (e: Throwable) {
                        setSnackbarMsg("Fout bij verwijderen: $e")
                        setSnackbarOpen(true)
                    }
                }
            }
            Unit
        }
    }

    ProductPage {
        this.items = merged.unsafeCast<List<ProductView>>()
        this.categories = categories
        this.loading = loading
        this.error = error
        this.onAdd = handleAdd
        this.onEdit = handleEdit
        this.onDelete = handleDelete
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