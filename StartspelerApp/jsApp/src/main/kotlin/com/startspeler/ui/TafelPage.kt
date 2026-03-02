package com.startspeler.ui

import mui.material.*
import mui.system.Box
import react.FC
import react.Props

external interface TafelView {
    var id: Int
    var number: Int
    var active: Boolean
}

external interface TafelPageProps : Props {
    var items: List<TafelView>
    var loading: Boolean
    var error: String?
    var onToggleActive: (id: Int, newActive: Boolean) -> Unit
}

val TafelPage = FC<TafelPageProps> { props ->
    Box {
        sx = js("{ width: '100vw', minHeight: 'calc(100vh - 60px)', display: 'flex', justifyContent: 'center', alignItems: 'flex-start', pt: 3, pb: 4, boxSizing: 'border-box' }")

        Box {
            sx = js("{ width: '100%', maxWidth: '1100px', px: 3, boxSizing: 'border-box' }")

            Typography { asDynamic().variant = "h6"; +"Tafels" }

            if (props.loading) {
                Box { sx = js("{ mt: 2 }"); CircularProgress {} }
            } else if (props.error != null) {
                Box { sx = js("{ mt: 2 }"); Alert { asDynamic().severity = "error"; +props.error!! } }
            } else {
                Box {
                    // gebruik CSS grid class uit tafel-card.css
                    asDynamic().className = "tafelGrid"

                    props.items
                        .sortedBy { it.number }
                        .forEach { t ->
                            Card {
                                key = t.id.toString()

                                // classes uit tafel-card.css
                                asDynamic().className =
                                    "tafelCard " + if (t.active) "tafelCard--active" else "tafelCard--inactive"

                                asDynamic().onClick = { props.onToggleActive(t.id, !t.active) }

                                CardContent {
                                    // CardContent zelf geen extra sx nodig; we stylen via classes op children
                                    Typography {
                                        asDynamic().component = "h3"
                                        asDynamic().className = "tafelCardTitle"
                                        +"Tafel ${t.number}"
                                    }
                                    Typography {
                                        asDynamic().component = "div"
                                        asDynamic().className = "tafelCardStatus"
                                        +if (t.active) "Actief" else "Inactief"
                                    }
                                }
                            }
                        }
                }
            }
        }
    }
}