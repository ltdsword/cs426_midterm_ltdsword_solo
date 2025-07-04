package com.example.codecup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView


class RewardFragment : Fragment() {

    private lateinit var profile: Profile

    private val profileManagement = ProfileManagement()
    private lateinit var rewardAdapter: RewardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reward, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // show the bottom navigation bar
        requireActivity().findViewById<MaterialCardView>(R.id.bottom_navi_bar)?.visibility = View.VISIBLE

        profile = profileManagement.getProfileFromLocal(requireContext())

        val ltyPts = view.findViewById<TextView>(R.id.ltyid)
        val cups = listOf(R.id.c1, R.id.c2, R.id.c3, R.id.c4, R.id.c5, R.id.c6, R.id.c7, R.id.c8)
        val cupImages = cups.map { view.findViewById<ImageView>(it) }

        val ltypts = profile.loyaltyPts
        ltyPts.text = "$ltypts / 8"

        for (i in 0 until 8) {
            if (i < ltypts) {
                cupImages[i].alpha = 1.0f
            }
            else {
                cupImages[i].alpha = 0.4f
            }
        }


        val pts =  profile.points
        val totalPoints = view.findViewById<TextView>(R.id.totalPoints)
        totalPoints.text = "$pts"

        val redeemButton = view.findViewById<TextView>(R.id.redeemButton)
        redeemButton.setOnClickListener {
            // go to Redeem fragment
            parentFragmentManager.beginTransaction()
                // this is for smooth transition
                .setCustomAnimations(
                    R.anim.slide_in_right, // enter
                    R.anim.slide_out_left, // exit
                    R.anim.slide_in_left,  // popEnter
                    R.anim.slide_out_right // popExit
                )
                .replace(R.id.fragment_container, RedeemFragment())
                .addToBackStack(null)
                .commit()
        }

        val totalOrder = profile.ongoing.hist + profile.history.hist

        // sort by date
        val sortedOrder = totalOrder.toMutableList().sortedByDescending { it.date }

        val rewardRecycler = view.findViewById<RecyclerView>(R.id.coffeeRecyclerView)
        rewardRecycler.layoutManager = LinearLayoutManager(requireContext())
        rewardAdapter = RewardAdapter(sortedOrder)
        rewardRecycler.adapter = rewardAdapter
    }

    override fun onStop() {
        super.onStop()
        // save the profile data
        profileManagement.saveProfile(profile, requireContext())
    }
}


