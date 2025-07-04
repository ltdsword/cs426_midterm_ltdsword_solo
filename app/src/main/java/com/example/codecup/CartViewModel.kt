package com.example.codecup

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// use a Shared View model to manipulate a cart in fragments
// they can modify the cart to display in CartFragment
class CartViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<MutableList<Coffee>>(mutableListOf())
    val cartItems: LiveData<MutableList<Coffee>> = _cartItems
    private lateinit var sharedPrefs: SharedPreferences

    val totalPrice: LiveData<Double> = cartItems.map { cart ->
        cart.sumOf { coffee -> coffee.price * coffee.qty }
    }

    fun notifyChange() {
        _cartItems.value = _cartItems.value?.toMutableList()
    }

    fun initSharedPrefs(context: Context) {
        sharedPrefs = context.getSharedPreferences("CartPrefs", Application.MODE_PRIVATE)
    }

    fun addItem(coffee: Coffee) {
        _cartItems.value?.let { list ->
            list.add(coffee)
            _cartItems.value = list.toMutableList()
        }
    }

    fun updateItem(old: Coffee, new: Coffee) {
        _cartItems.value?.let { list ->
            val index = list.indexOf(old)
            if (index != -1) {
                list[index] = new
                _cartItems.value = list.toMutableList()
            }
        }
    }

    fun removeItem(coffee: Coffee) {
        _cartItems.value?.let {
            it.remove(coffee)
            _cartItems.value = it.toMutableList()
        }
    }

    fun saveCartToPrefs() {
        val json = Gson().toJson(_cartItems.value ?: emptyList<Coffee>())
        Log.d("CartViewModel", "Saving cart to SharedPreferences: $json")
        sharedPrefs.edit().putString("cart", json).apply()
    }

    fun loadCartFromPrefs() {
        val json = sharedPrefs.getString("cart", null)
        Log.d("CartViewModel", "Loading cart from SharedPreferences: $json")
        if (json != null) {
            val type = object : TypeToken<MutableList<Coffee>>() {}.type
            val list: MutableList<Coffee> = Gson().fromJson(json, type)
            _cartItems.value = list
            delCartInPrefs()
        }
    }

    private fun delCartInPrefs() {
        sharedPrefs.edit().remove("cart").apply()
    }

    override fun onCleared() {
        super.onCleared()
        saveCartToPrefs()
    }

}