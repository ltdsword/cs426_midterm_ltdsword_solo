package com.example.codecup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView


data class RedeemItem(
    val name: String,
    val validUntil: String,
    val imageResId: Int,
    val requiredPoints: Int
)

class RedeemFragment : Fragment() {

    private lateinit var username: String
    private lateinit var profile: Profile

    private val profileManagement = ProfileManagement()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_redeem, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // hide the bottom navigation bar
        requireActivity().findViewById<MaterialCardView>(R.id.bottom_navi_bar)?.visibility =
            View.GONE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })

        profile = profileManagement.getProfileFromLocal(requireContext())

        // back button
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.coffeeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Build list of redeemable coffees with points
        val coffeeList = getCoffeeList()
        val redeemMap = getRedeemPointsNeeded()
        val redeemItems = coffeeList.map {
            RedeemItem(
                name = it.name,
                validUntil = "10.01.25",
                imageResId = it.imageResId,
                requiredPoints = redeemMap[it.name] ?: 0
            )
        }

        // Set adapter
        recyclerView.adapter = RedeemAdapter(redeemItems)
    }
}











