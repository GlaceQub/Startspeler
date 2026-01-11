package com.startspeler

import com.startspeler.dto.ProductItem
import mui.system.Box
import react.FC
import react.Props
import kotlin.js.js

external interface ProductCardBProps : Props {
    var item: ProductItem
}

val ProductCardB = FC<ProductCardBProps> { props ->
    Box {
        sx = js("""{ backgroundColor: 'lightgreen', borderRadius: '12px', padding: '16px', boxShadow: '0 2px 8px #c8e6c9', display: 'flex', flexDirection: 'column', alignItems: 'center' }""")
        +props.item.name
        // Add more fields as needed
    }
}

