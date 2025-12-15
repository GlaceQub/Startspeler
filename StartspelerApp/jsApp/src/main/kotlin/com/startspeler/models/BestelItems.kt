package com.startspeler.models

object BestelItems {
    val drinks = listOf(
        BestelItem(id = "cola", name = "Cola", price = "€ 2,10"),
        BestelItem(id = "colaZ", name = "Cola Zero", price = "€ 2,10"),
        BestelItem(id = "fanta", name = "Fanta", price = "€ 2,10"),
        BestelItem(id = "ice_tea", name = "Ice-Tea", price = "€ 2,30"),
        BestelItem(id = "agrum", name = "Agrum", price = "€ 2,30"),
        BestelItem(id = "nalu", name = "Nalu", price = "€ 3,00"),
        BestelItem(id = "redbull", name = "Redbull", price = "€ 3,50"),
        BestelItem(id = "toniss", name = "Tönissteiner", price = "Out Of Stock", outOfStock = true)
    )
}
