package com.startspeler.components.bestellingen

import com.startspeler.dto.OrderOverzichtItem
import mui.material.Accordion
import mui.material.AccordionDetails
import mui.material.AccordionSummary
import mui.material.Box
import mui.material.Typography
import mui.icons.material.ExpandMore
import react.FC
import react.Props
import react.create
import react.useState

external interface OrderOverzichtItemProps : Props {
    var order: OrderOverzichtItem
    var isOpen: Boolean
}

val OrderOverzichtItem = FC<OrderOverzichtItemProps> { props ->
    val order = props.order
    val (isOpen, setOpen) = useState(props.isOpen)

    Accordion {
        expanded = isOpen
        onChange = { _, expanded -> setOpen(expanded) }
        AccordionSummary {
            expandIcon = ExpandMore.create()
            Box {
                sx = js("{ display: 'flex', flexDirection: 'row', gap: '16px', alignItems: 'left', width: '100%' }")
                Typography {
                    variant = mui.material.styles.TypographyVariant.h6
                    +"${order.id}"
                }
                Typography {
                    variant = mui.material.styles.TypographyVariant.h6
                    +"Tafel ${order.tableNumber}"
                }
                Typography {
                    variant = mui.material.styles.TypographyVariant.h6
                    +order.clientName
                }
                Typography {
                    variant = mui.material.styles.TypographyVariant.h6
                    +order.status
                }
            }
        }
        AccordionDetails {
            Typography {
                +"Totaal: € ${order.totalPrice}"
            }
            order.orderitems.forEach { item ->
                Typography {
                    +"${item.product} x ${item.quantity} (€ ${item.price})"
                }
            }
        }
    }
}