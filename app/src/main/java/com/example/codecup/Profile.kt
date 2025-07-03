package com.example.codecup

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import java.util.Date

@Keep
@Parcelize
data class Coffee(val name: String,
                  val imageResId: Int,
                  var qty: Int = 1,
                  var price: Double = 0.00,
                  var single: Boolean = true,
                  var hot: Boolean = true,
                  var size: Int = 0,
                  var ice: Int = -1,
                  var priceSmall: Double = 0.00,
                  var priceMed: Double = 0.00,
                  var priceBig: Double = 0.00
                  ): Parcelable

@Keep
data class Order(val name: String = "",
                 val price: Double = 0.00,
                 val qty: Int = 1,
                 val loyaltyPts: Int = 30,
                 val date: Timestamp = Timestamp.now(),
                 var address: String = "")

@Keep
data class History(
    var hist: List<Order> = listOf()
    ) {
    @Exclude
    fun addObject(name: String, price: Double, qty: Int, ltypts: Int, address: String) {
        hist = hist + Order(name, price, qty, ltypts, Timestamp.now(), address)
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
    var address: String = "",
    var email: String = "",
    var history: History = History(),
    var ongoing: History = History(),
    var points: Int = 0,
    var loyaltyPts: Int = 0,
    var avatarUrl: String = "",
    var avatarUrlSmall: String = ""
)