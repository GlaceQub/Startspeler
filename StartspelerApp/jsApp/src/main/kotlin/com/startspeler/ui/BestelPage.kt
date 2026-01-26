package com.startspeler.ui

import mui.system.Box
import react.FC
import com.startspeler.dto.ProductItem
import com.startspeler.dto.CartItem
import com.startspeler.models.Category


external interface BestelPageProps : react.Props {
    var products: List<ProductItem>
    var categories: List<Category>
    var onCategoryClick: (Category) -> Unit
    var selectedCategory: Category?
    var onBackClick: () -> Unit
    var loading: Boolean // Add loading prop
    var error: String? // Add error prop
    var cartItems: List<CartItem>
    var onAddToCart: (ProductItem) -> Unit
    var onRemoveFromCart: (CartItem) -> Unit
    var OnOrder: () -> Unit
    var tafelOptions: List<String>
    var selectedTafel: String
    var onTafelChange: (String) -> Unit
    var klantOptions: List<String>
    var selectedKlant: String
    var onKlantChange: (String) -> Unit
    var onAddKlant: () -> Unit
}

val BestelPage = FC<BestelPageProps> { pageProps ->
    console.log("[BestelPage] props:", pageProps)
    Box {
        sx = js("{ display: 'flex', flexDirection: 'row', width: '100%', minHeight: '80vh', boxSizing: 'border-box', overflowX: 'hidden' }")
        Box {
            sx = js("{ flex: '0 0 70%', paddingRight: '24px', boxSizing: 'border-box', minWidth: 0 }")
            Menu {
                products = pageProps.products
                categories = pageProps.categories
                selectedCategory = pageProps.selectedCategory
                onCategoryClick = pageProps.onCategoryClick
                onBackClick = pageProps.onBackClick
                onAddToCart = pageProps.onAddToCart
                loading = pageProps.loading
                error = pageProps.error
            }
        }
        Box {
            sx = js("{ flex: '0 0 30%', paddingLeft: '24px', boxSizing: 'border-box', minWidth: 0 }")
            console.log("[BestelPage] cartItems:", pageProps.cartItems)
            Cart {
                this.cartItems = pageProps.cartItems
                this.onRemove = pageProps.onRemoveFromCart
                this.onOrder = pageProps.OnOrder
                this.tafelOptions = pageProps.tafelOptions
                this.selectedTafel = pageProps.selectedTafel
                this.onTafelChange = pageProps.onTafelChange
                this.klantOptions = pageProps.klantOptions
                this.selectedKlant = pageProps.selectedKlant
                this.onKlantChange = pageProps.onKlantChange
                this.onAddKlant = pageProps.onAddKlant
            }
        }
    }

}