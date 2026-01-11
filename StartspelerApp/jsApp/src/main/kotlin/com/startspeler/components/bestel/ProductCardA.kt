package com.startspeler

import com.startspeler.dto.ProductItem
import mui.system.Box
import react.FC
import react.Props
import kotlin.js.js

external interface ProductCardAProps : Props {
    var item: ProductItem
}

val ProductCardA = FC<ProductCardAProps> { props ->
    Box {
        sx = js("""{ backgroundColor: 'lightblue', borderRadius: '12px', padding: '16px', boxShadow: '0 2px 8px #b3e5fc', display: 'flex', flexDirection: 'column', alignItems: 'center' }""")
        +props.item.name
        // Add more fields as needed
    }
}

