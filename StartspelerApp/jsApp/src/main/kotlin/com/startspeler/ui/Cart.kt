package com.startspeler.ui

import mui.material.Paper
import mui.material.Typography
import mui.material.IconButton
import mui.icons.material.Delete
import mui.system.Box
import react.FC
import react.Props
import com.startspeler.dto.CartItem
import mui.material.Button

external interface CartProps : Props {
    var cartItems: List<CartItem>
    var onRemove: (CartItem) -> Unit
    var onOrder: () -> Unit
    var tafelOptions: List<String>
    var selectedTafel: String
    var onTafelChange: (String) -> Unit
    var klantOptions: List<String>
    var selectedKlant: String
    var onKlantChange: (String) -> Unit
    var onAddKlant: () -> Unit
    var submitLabel: String?
    var isSubmitting: Boolean?
    var conflictingProductNames: List<String>?
}

val Cart = FC<CartProps> { props ->
    val isSubmitting = props.isSubmitting ?: false
    val conflictingProductNames = props.conflictingProductNames ?: emptyList()

    Box {
        sx = js("{padding: '24px',display: 'flex', flexDirection: 'column', justifyContent: 'flex-start' }")
        Paper {
            sx =
                js("{backgroundColor: 'var(--startspeler-secondary)', padding: '16px', height: '100%', display: 'flex', flexDirection: 'column', gap: '16px', boxShadow: '0px 2px 8px 0px rgba(0,0,0,0.10)', boxSizing: 'border-box', borderRadius: '20px' }")
            Typography {
                variant = mui.material.styles.TypographyVariant.h6
                sx = js("{ fontWeight: 700, marginBottom: '16px' }")
                +"Winkelwagen"
            }
            val items = props.cartItems
            if (items.isEmpty()) {
                Typography { +"Je winkelwagen is leeg." }
            } else {
                Box {
                    sx =
                        js("{ display: 'flex', flexDirection: 'column', gap: '8px', overflowY: 'auto', maxHeight: '60vh' }")
                    items.forEach { item ->
                        val hasConflict = conflictingProductNames.contains(item.product.name)
                        Box {
                            sx =
                                js("{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '8px' }")
                            Box {
                                sx = js("{ display: 'flex', flexDirection: 'column', gap: '2px' }")
                                Typography {
                                    sx = js("{ fontWeight: 500 }")
                                    +"${item.product.name} x${item.quantity}"
                                }
                                if (hasConflict) {
                                    Typography {
                                        sx = js("{ color: '#555', fontSize: '0.85rem', fontWeight: 500 }")
                                        +"Aangepast door systeem"
                                    }
                                }
                            }
                            IconButton {
                                onClick = { _ -> props.onRemove(item) }
                                Delete {}
                            }
                        }
                    }
                }
            }
        }

        Typography {
            sx = js("{ fontWeight: 700, marginTop: '16px', marginBottom: '8px' }")
            +"Tafel"
        }
        mui.material.Select {
            value = props.selectedTafel
            onChange = { event, _ -> props.onTafelChange(event.target.value) }
            fullWidth = true
            props.tafelOptions.forEach { tafel ->
                mui.material.MenuItem {
                    value = tafel
                    +tafel
                }
            }
        }
        Typography {
            sx = js("{ fontWeight: 700, marginTop: '16px', marginBottom: '8px' }")
            +"Klant"
        }
        Box {
            sx = js("{ display: 'flex', alignItems: 'center', gap: '8px' }")
            mui.material.Select {
                value = props.selectedKlant
                onChange = { event, _ -> props.onKlantChange(event.target.value) }
                fullWidth = true
                props.klantOptions.forEach { klant ->
                    mui.material.MenuItem {
                        value = klant
                        +klant
                    }
                }
            }
            Button {
                asDynamic().className = "app-button"
                sx = js("{ minWidth: '40px', height: '40px', fontSize: '1.5rem' }")
                onClick = { _ -> props.onAddKlant() }
                +"+"
            }
        }

        Button {
            sx =
                js("{ marginTop: 'auto', marginTop: '16px', backgroundColor: 'var(--startspeler-primary)', color: 'white', fontWeight: 700, borderRadius: '20px' }")
            fullWidth = true
            disabled = props.cartItems.isEmpty() || isSubmitting
            +(props.submitLabel ?: "Bestellen")
            onClick = { _ -> props.onOrder() }
        }
    }
}
