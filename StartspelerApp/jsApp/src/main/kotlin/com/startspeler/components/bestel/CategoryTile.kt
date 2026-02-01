package com.startspeler.components.bestel

import com.startspeler.models.Category
import mui.material.Button
import mui.material.Typography
import mui.material.Box
import mui.material.styles.TypographyVariant
import react.FC
import react.Props

external interface CategoryTileProps : Props {
    var category: Category
    var onClick: (Category) -> Unit
}

val CategoryTile = FC<CategoryTileProps> { props ->
    Button {
        asDynamic().className = "categoryTile"
        sx = js("{ width: { xs: '70vw', sm: '220px', md: '260px', lg: '300px', xl: '340px' }, height: { xs: '100px', sm: '160px', md: '180px', lg: '220px', xl: '240px' }, maxWidth: '100%' }")
        disableElevation = true
        fullWidth = true
        onClick = { props.onClick(props.category) }
        Box {
            sx = js("{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 0 }")
            Typography {
                variant = TypographyVariant.h4
                sx = js("{ width: '100%', textAlign: 'center', fontWeight: 700, color: 'white', textTransform: 'none' }")
                +props.category.name
            }
        }
    }
}