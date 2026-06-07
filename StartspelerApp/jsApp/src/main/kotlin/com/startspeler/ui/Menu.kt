package com.startspeler.ui

import mui.system.Box
import mui.material.Typography
import mui.material.Button
import mui.icons.material.ArrowBackIosNew as Back
import com.startspeler.dto.ProductItem
import com.startspeler.models.Category
import com.startspeler.ProductCard
import com.startspeler.components.bestel.CategoryTile
import react.FC
import react.Props

external interface MenuProps : Props {
    var products: List<ProductItem>
    var categories: List<Category>
    var selectedCategory: Category?
    var onCategoryClick: (Category) -> Unit
    var onBackClick: () -> Unit
    var onAddToCart: (ProductItem) -> Unit
    var loading: Boolean // Add loading prop
    var error: String?
}

val Menu = FC<MenuProps> { props ->
    if (props.error != null) {
        mui.material.Typography {
            sx = js("{ color: 'red', fontWeight: 700, margin: '16px', textAlign: 'center' }")
            +(props.error ?: "")
        }
    }
    if (props.loading) {
        Box {
            sx = js("{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }")
            mui.material.CircularProgress {}
        }
        return@FC
    }
    if (props.selectedCategory == null) {
        Box {
            sx = js("{ height: '56px' }") // Reserve space for back button
        }
        if (props.categories.isEmpty()) {
            Typography { +"Geen categorieën gevonden." }
        } else {
            Box {
                sx = js(
                    """{
                    display: 'flex',
                    flexWrap: 'wrap',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: '24px',
                    padding: '24px'
                }"""
                )
                props.categories.forEach { category ->
                    CategoryTile {
                        this.category = category
                        this.onClick = props.onCategoryClick
                    }
                }
            }
        }
    } else {
        Button {
            sx = js("""{ margin: '16px', width: 'fit-content' }""")
            onClick = { props.onBackClick() }
            Box {
                sx = js("{ display: 'flex', alignItems: 'center' }")
                Back {
                    sx = js("{color: 'var(--startspeler-primary)', marginRight: '8px' }")
                }
                Typography {
                    sx = js("{ color: 'var(--startspeler-primary)', fontWeight: 700, textTransform: 'none' }")
                    +"Terug"
                }
            }
        }
        if (props.products.isEmpty()) {
            Typography { +"Geen producten gevonden voor deze categorie." }
        } else {
            Box {
                sx = js(
                    """{
                    display: 'flex',
                    flexWrap: 'wrap',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: '24px',
                    padding: '24px'
                }"""
                )
                props.products.forEach { item ->
                    console.log("Product displayed", item)
                    ProductCard {
                        this.item = item
                        this.onClick = { product ->
                            props.onAddToCart(product)
                        }
                    }
                }
            }
        }
    }
}
