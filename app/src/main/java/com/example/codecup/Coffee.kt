package com.example.codecup

import com.example.cuoi.Coffee

fun getCoffeeList(): List<Coffee> {
    val coffeeList = listOf(
        Coffee("Americano", R.mipmap.ic_americano_foreground),
        Coffee("Cappuccino", R.mipmap.ic_cappuccino_foreground),
        Coffee("Latte", R.mipmap.ic_latte_foreground),
        Coffee("Mocha", R.mipmap.ic_mocha_foreground),
        Coffee("Espresso", R.mipmap.ic_espresso_foreground),
        Coffee("Flat White", R.mipmap.ic_flatwhite_foreground),
        Coffee("Hot Chocolate", R.mipmap.ic_hotchoco_foreground),
        Coffee("Cortado", R.mipmap.ic_cortado_foreground)
    )

    return coffeeList
}

fun getRedeemPointsNeeded(): Map<String, Int> {
    val redeemPoints = mapOf(
        "Americano" to 300,
        "Cappuccino" to 300,
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
        "Americano" to 30,
        "Cappuccino" to 30,
        "Latte" to 30,
        "Mocha" to 30,
        "Espresso" to 35,
        "Flat White" to 35,
        "Hot Chocolate" to 40,
        "Cortado" to 40
    )
    return rp
}
