package com.startspeler.ui

import mui.system.Box
import mui.material.Typography
import react.FC
import react.Props
import kotlin.js.js
import com.startspeler.dto.ProductItem
import com.startspeler.models.Category
import com.startspeler.components.bestel.CategoryTile

external interface BestelPageProps : Props {
    var products: List<ProductItem>
    var categories: List<Category>
    var onCategoryClick: (Category) -> Unit
    var selectedCategory: Category?
}

val BestelPage = FC<BestelPageProps> { props ->
    Box {
        // Remove margin and height restrictions if navbar is not fixed
        if (props.selectedCategory == null) {
            // Show category tiles
            if (props.categories.isEmpty()) {
                Typography { +"Geen categorieën gevonden." }
            } else {
                Box {
                    sx = js("""{ display: 'grid', gridTemplateColumns: 'repeat(4, auto)', gap: '24px', padding: '24px', justifyContent: 'center', alignItems: 'center' }""")
                    props.categories.forEach { category ->
                        CategoryTile {
                            this.category = category
                            this.onClick = props.onCategoryClick
                        }
                    }
                }
            }
        } else {
            // Show products for selected category
            if (props.products.isEmpty()) {
                Typography { +"Geen producten gevonden voor deze categorie." }
            } else {
                Box {
                    sx = js("""{ display: 'grid', gridTemplateColumns: 'repeat(4, auto)', gridAutoFlow: 'row', justifyContent: 'center', gap: '24px', padding: '24px', alignItems: 'center' }""")
                    props.products.forEach { item ->
                        Typography {
                            +"${'$'}{item.name} - €${'$'}{item.price}"
                        }
                    }
                }
            }
        }

    }
}