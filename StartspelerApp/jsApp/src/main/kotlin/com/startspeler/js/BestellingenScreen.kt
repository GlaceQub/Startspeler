package com.startspeler.js

import com.startspeler.components.bestellingen.OrderOverzichtItem
import com.startspeler.dto.OrderOverzichtItem
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import mui.material.Box
import mui.material.CircularProgress
import mui.material.Typography
import react.FC
import react.Props
import react.useEffectOnce
import react.useState

val BestellingenScreen = FC<Props> {
    val (orders, setOrders) = useState<List<OrderOverzichtItem>>(emptyList())
    val (loading, setLoading) = useState(true)
    val (error, setError) = useState<String?>(null)

    useEffectOnce {
        MainScope().launch {
            try {
                val configResponse = window.fetch("/config.json").await()
                val config = configResponse.json().await().asDynamic()
                val backendUrl = config.backendUrl as? String ?: ""

                val response = window.fetch("$backendUrl/order/all").await()
                if (response.ok) {
                    val json = Json { ignoreUnknownKeys = true }
                    val text = response.text().await()
                    val parsed = json.decodeFromString<List<OrderOverzichtItem>>(text)
                    setOrders(parsed)
                } else {
                    setError("Fout bij ophalen orders: ${response.status}")
                }
            } catch (e: Throwable) {
                setError("Fout: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    Box {
        sx = js("{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '16px', width: '100%', boxSizing: 'border-box' }")

        if (loading) {
            CircularProgress {}
        } else if (error != null) {
            Typography { + "Er is iets fout gegaan bij het ophalen van de bestellingen!" }
        } else {
            orders.forEach { order ->
                OrderOverzichtItem {
                    this.order = order
                    this.isOpen = false
                }
            }
        }
    }
}