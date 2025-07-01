package com.example.codecup

fun getCoffeeList(): List<Coffee> {
    val coffeeList = listOf(
        Coffee("Americano", R.mipmap.ic_americano_foreground, price = 2.50),
        Coffee("Cappuccino", R.mipmap.ic_cappuccino_foreground, price = 2.50),
        Coffee("Latte", R.mipmap.ic_latte_foreground, price = 3.00),
        Coffee("Mocha", R.mipmap.ic_mocha_foreground, price = 3.00),
        Coffee("Espresso", R.mipmap.ic_espresso_foreground, price = 3.50),
        Coffee("Flat White", R.mipmap.ic_flatwhite_foreground, price = 3.50),
        Coffee("Hot Chocolate", R.mipmap.ic_hotchoco_foreground, price = 4.00),
        Coffee("Cortado", R.mipmap.ic_cortado_foreground, price = 4.00)
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

fun getRedeemPoints(): Map<String, Int> {
    val rp = mapOf(
        "Americano" to 25,
        "Cappuccino" to 25,
        "Latte" to 30,
        "Mocha" to 30,
        "Espresso" to 35,
        "Flat White" to 35,
        "Hot Chocolate" to 40,
        "Cortado" to 40
    )
    return rp
}
