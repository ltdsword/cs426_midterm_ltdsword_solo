package com.example.cuoi

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.Date

@Keep
data class Coffee(val name: String, val imageResId: Int, var price: Double = 0.00)

@Keep
data class Order(val name: String, val price: Int, val qty: Int, val loyaltyPts : Int, val date: Timestamp)

@Keep
data class History(
    var hist: List<Order> = listOf()
    ) {
    @Exclude
    fun addObject(date: String, name: String, price: Int, qty: Int, ltypts: Int) {
        hist = hist + Order(name, price, qty, ltypts, Timestamp(Date(date)))
    }

    @Exclude
    fun clear() {
        hist = emptyList()
    }

    @Exclude
    fun getList(): List<Order> {
        return hist
    }
}

@Keep
data class Profile(
    var name: String = "",
    var password: String = "",
    var age: Int = 0,
    var phoneNumber: String = "",
    var email: String = "",
    var history: History = History(),
    val points: Int = 0,
    val loyaltyPts: Int = 0,
    var avatarUrl: String = "",
    var avatarUrlSmall: String = ""
)