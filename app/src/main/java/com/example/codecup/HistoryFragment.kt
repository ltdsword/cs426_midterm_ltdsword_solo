package com.example.codecup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HistoryFragment : Fragment() {

    lateinit var histAdapter: OrderAdapter
    private lateinit var profile: Profile

    private val profileManagement = ProfileManagement()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_order_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val profileManagement = ProfileManagement()
        profile = profileManagement.getProfileFromLocal(requireContext())

        histAdapter = OrderAdapter(profile.history.hist.toMutableList(), false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.orderRecyclerView)
        recyclerView.adapter = histAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        val updated = profileManagement.getProfileFromLocal(requireContext()).history.hist
        histAdapter.updateData(updated)
    }
}