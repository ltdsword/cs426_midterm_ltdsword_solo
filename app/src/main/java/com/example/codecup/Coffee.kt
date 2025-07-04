package com.example.codecup

fun getCoffeeList(): List<Coffee> {
    val coffeeList = listOf(
        Coffee("Americano", R.mipmap.ic_americano_foreground, price = 2.50, priceSmall = 2.50, priceMed = 3.00, priceBig = 3.50),
        Coffee("Cappuccino", R.mipmap.ic_cappuccino_foreground, price = 2.50, priceSmall = 2.50, priceMed = 3.00, priceBig = 3.50),
        Coffee("Latte", R.mipmap.ic_latte_foreground, price = 3.00, priceSmall = 3.00, priceMed = 3.50, priceBig = 4.00),
        Coffee("Mocha", R.mipmap.ic_mocha_foreground, price = 3.00, priceSmall = 3.00, priceMed = 3.50, priceBig = 4.00),
        Coffee("Espresso", R.mipmap.ic_espresso_foreground, price = 3.50, priceSmall = 3.50, priceMed = 4.00, priceBig = 4.50),
        Coffee("Flat White", R.mipmap.ic_flatwhite_foreground, price = 3.50, priceSmall = 3.50, priceMed = 4.00, priceBig = 4.50),
        Coffee("Hot Chocolate", R.mipmap.ic_hotchoco_foreground, price = 4.00, priceSmall = 4.00, priceMed = 4.50, priceBig = 5.00),
        Coffee("Cortado", R.mipmap.ic_cortado_foreground, price = 4.00, priceSmall = 4.00, priceMed = 4.50, priceBig = 5.00)
    )

    return coffeeList
}

fun getRedeemPointsNeeded(): Map<String, Int> {
    val redeemPoints = mapOf(
        "Americano" to 250,
        "Cappuccino" to 250,
        "Latte" to 300,
        "Mocha" to 300,
        "Espresso" to 350,
        "Flat White" to 350,
        "Hot Chocolate" to 400,
        "Cortado" to 400
    )
    return redeemPoints
}

fun getRedeemPoints(price: Double) : Int {
    return (price * 10.0).toInt()
}
