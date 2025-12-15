package com.startspeler.ui

import com.startspeler.models.BestelItems
import com.startspeler.models.BestelItem
import mui.system.Box
import react.FC
import react.Props
import kotlin.js.js

val BestelPage = FC<Props> {
    Box {
        sx = js("""{ display: 'grid', gridTemplateColumns: 'repeat(4, auto)', gridTemplateRows: 'repeat(2, auto)', gridAutoFlow: 'row', justifyContent: 'center', gap: '24px', padding: '24px', alignItems: 'center' }""")
        BestelItems.drinks.forEach { item: BestelItem ->
            Box {
                key = item.id
                sx = js("""{display: 'flex', justifyContent: 'center', marginBottom: '24px' }""")
                ProductButton {
                    label = item.name
                    price = item.price
                    disabled = item.outOfStock
                    onClick = {
                        console.log("Clicked", item.id, item.name)
                    }
                }
            }
        }

    }
}