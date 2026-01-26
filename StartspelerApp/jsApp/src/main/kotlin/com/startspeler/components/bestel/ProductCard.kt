package com.startspeler

import com.startspeler.dto.ProductItem
import mui.icons.material.Add
import mui.material.Button
import mui.material.Typography
import mui.system.Box
import react.FC
import react.Props
import kotlin.js.js

external interface ProductCardProps : Props {
    var item: ProductItem
    var onClick: (ProductItem) -> Unit?
}

val ProductCard = FC<ProductCardProps> { props ->
    val outOfStock = props.item.outOfStock
    Button {
        asDynamic().className = "productCard"
        sx = js("{ width: { xs: '160px', sm: '180px', md: '200px', lg: '220px', xl: '240px' }, height: { xs: '110px', sm: '120px', md: '130px', lg: '140px', xl: '150px' }, maxWidth: '100%' }")
        disabled = outOfStock
        classes = if (outOfStock) js("{ root: 'Mui-disabled' }") else undefined
        disableElevation = true
        fullWidth = true
        onClick = {
            if (!outOfStock) props.onClick(props.item)
        }
        Box {
            sx = js("{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', width: '100%', gap: '6px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h5
                sx = js("{ color: 'white', fontWeight: 700, textAlign: 'center', width: '100%', textTransform: 'none' }")
                +props.item.name
            }
            Typography {
                variant = mui.material.styles.TypographyVariant.h6
                sx = js("{ color: 'white', fontWeight: 700, textAlign: 'center', width: '100%', textTransform: 'none' }")
                val priceFloat = props.item.price
                val priceString = try {
                    val jsNumber = priceFloat.asDynamic()
                    val fixed = jsNumber.toFixed(2) as String
                    "€ " + fixed.replace('.', ',')
                } catch (e: Exception) {
                    "€ 0,00"
                }
                +priceString
            }
            if (outOfStock) {
                Typography {
                    sx = js("{ color: 'white', fontWeight: 700, textAlign: 'center', width: '100%', fontSize: '1.2rem', textTransform: 'none' }")
                    +"Niet beschikbaar"
                }
            } else {
                Add {
                    sx = js("{ color: 'white', fontSize: '2rem', fontWeight: 700 }")
                }
            }
        }
    }
}
