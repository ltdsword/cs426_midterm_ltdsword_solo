package com.example.codecup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CartBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cart_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Shared ViewModel across fragments
        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]
        val cartItems = cartViewModel.cartItems.value ?: listOf()

        val recyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)

        // Adapter setup
        adapter = CartAdapter(cartItems, cartViewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
}
