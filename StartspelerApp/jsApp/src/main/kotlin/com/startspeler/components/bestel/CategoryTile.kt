package com.startspeler.components.bestel

import com.startspeler.models.Category
import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import react.FC
import react.Props

external interface CategoryTileProps : Props {
    var category: Category
    var onClick: (Category) -> Unit
}

val CategoryTile = FC<CategoryTileProps> { props ->
    Card {
        onClick = { props.onClick(props.category) }
        CardContent {
            Typography {
                +props.category.name
            }
        }
    }
}