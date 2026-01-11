package com.startspeler.js

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

external interface BestelScreenProps : Props

val BestelScreen = FC<BestelScreenProps> {
    val (categories, setCategories) = useState<List<Category>>(emptyList())
    val (products, setProducts) = useState<List<ProductItem>>(emptyList())
    val (selectedCategory, setSelectedCategory) = useState<Category?>(null)
    val (backendUrl, setBackendUrl) = useState<String?>(null)
    val (error, setError) = useState<String?>(null)

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
            MainScope().launch {
                try {
                    val response = window.fetch(backendUrl.trimEnd('/') + "/categories")
                        .await()
                        .text()
                        .await()
                    val categories = Json.decodeFromString<List<Category>>(response)
                    setCategories(categories)
                } catch (e: Throwable) {
                    setError("Failed to fetch categories: ${'$'}e")
                }
            }
        }
    }

    val handleCategoryClick: (Category) -> Unit = { category ->
        setSelectedCategory(category)
        if (backendUrl != null) {
            MainScope().launch {
                try {
                    val response = window.fetch(backendUrl.trimEnd('/') + "/products/category/${category.id}/with-stock")
                        .await()
                        .text()
                        .await()
                    val products = Json.decodeFromString<List<ProductItem>>(response)
                    setProducts(products)
                } catch (e: Throwable) {
                    setError("Failed to fetch products: ${'$'}e")
                }
            }
        }
    }

    if (error != null) {
        // Show error message if config or fetch fails
        react.dom.html.ReactHTML.div {
            +error
        }
        return@FC
    }

    BestelPage {
        this.categories = categories
        this.products = products
        this.selectedCategory = selectedCategory
        this.onCategoryClick = handleCategoryClick
        this.onBackClick = {
            setSelectedCategory(null)
            setProducts(emptyList())
        }
    }
}
