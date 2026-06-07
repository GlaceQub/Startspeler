package com.startspeler.ui

import mui.system.Box
import mui.material.Drawer
import mui.material.DrawerAnchor
import mui.material.Fab
import mui.material.Badge
import mui.icons.material.ShoppingCart
import react.FC
import react.useState
import com.startspeler.dto.ProductItem
import com.startspeler.dto.CartItem
import com.startspeler.models.Category


external interface BestelPageProps : react.Props {
    var products: List<ProductItem>
    var categories: List<Category>
    var onCategoryClick: (Category) -> Unit
    var selectedCategory: Category?
    var onBackClick: () -> Unit
    var loading: Boolean
    var error: String?
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
    var onOrderSubmit: ((List<CartItem>, String, String) -> Unit)?
    var submitLabel: String?
    var isSubmitting: Boolean?
    var conflictingProductNames: List<String>?
}

val BestelPage = FC<BestelPageProps> { pageProps ->
    val isSubmitting = pageProps.isSubmitting ?: false
    val conflictingProductNames = pageProps.conflictingProductNames ?: emptyList()
    val (mobileCartOpen, setMobileCartOpen) = useState(false)
    val totalCartItems = pageProps.cartItems.sumOf { it.quantity }

    val cartContent = FC<react.Props> {
        Cart {
            this.cartItems = pageProps.cartItems
            this.onRemove = pageProps.onRemoveFromCart
            this.onOrder = {
                val submit = pageProps.onOrderSubmit
                if (submit != null) {
                    submit(pageProps.cartItems, pageProps.selectedTafel, pageProps.selectedKlant)
                } else {
                    pageProps.OnOrder()
                }
                setMobileCartOpen(false)
            }
            this.tafelOptions = pageProps.tafelOptions
            this.selectedTafel = pageProps.selectedTafel
            this.onTafelChange = pageProps.onTafelChange
            this.klantOptions = pageProps.klantOptions
            this.selectedKlant = pageProps.selectedKlant
            this.onKlantChange = pageProps.onKlantChange
            this.onAddKlant = pageProps.onAddKlant
            this.submitLabel = pageProps.submitLabel
            this.isSubmitting = isSubmitting
            this.conflictingProductNames = conflictingProductNames
        }
    }

    Box {
        sx = js("{ position: 'relative', width: '100%', minHeight: '80vh', boxSizing: 'border-box' }")

        // ── Desktop / Tablet layout (sm and up): side-by-side ──────────────
        Box {
            sx = js("{ display: { xs: 'none', sm: 'flex' }, flexDirection: 'row', width: '100%', minHeight: '80vh', boxSizing: 'border-box', overflowX: 'hidden' }")
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
                cartContent {}
            }
        }

        // ── Mobile layout (xs only): full-width menu + floating cart FAB ───
        Box {
            sx = js("{ display: { xs: 'block', sm: 'none' }, width: '100%', paddingBottom: '80px', boxSizing: 'border-box' }")
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

        // ── Floating cart button (mobile only) ─────────────────────────────
        Box {
            sx = js("{ display: { xs: 'flex', sm: 'none' }, position: 'fixed', bottom: '24px', right: '24px', zIndex: 1200 }")
            Badge {
                badgeContent = react.ReactNode(if (totalCartItems > 0) totalCartItems.toString() else "")
                asDynamic().color = "error"
                Fab {
                    asDynamic().color = "primary"
                    asDynamic().sx = js("{ backgroundColor: 'var(--startspeler-primary)' }")
                    onClick = { _ -> setMobileCartOpen(true) }
                    ShoppingCart {}
                }
            }
        }

        // ── Bottom drawer (mobile cart) ─────────────────────────────────────
        Drawer {
            anchor = DrawerAnchor.bottom
            open = mobileCartOpen
            onClose = { _, _ -> setMobileCartOpen(false) }
            Box {
                sx = js("{ maxHeight: '85vh', overflowY: 'auto', borderRadius: '20px 20px 0 0' }")
                cartContent {}
            }
        }
    }
}