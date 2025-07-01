package com.example.codecup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HistoryFragment : Fragment() {

    private lateinit var orderAdapter: OrderAdapter
    private lateinit var profile: Profile

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_order_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val profileManagement = ProfileManagement()
        profile = profileManagement.getProfileFromLocal(requireContext())

        orderAdapter = OrderAdapter(profile.history.hist.toMutableList(), false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.orderRecyclerView)
        recyclerView.adapter = orderAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}