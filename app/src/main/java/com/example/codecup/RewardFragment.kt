package com.example.codecup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RewardFragment : Fragment() {

    private lateinit var username: String
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

        val rewardRecycler = view.findViewById<RecyclerView>(R.id.coffeeRecyclerView)
        rewardRecycler.layoutManager = LinearLayoutManager(requireContext())
        rewardAdapter = RewardAdapter(totalOrder)
        rewardRecycler.adapter = rewardAdapter
    }

    override fun onStop() {
        super.onStop()
        // save the profile data
        profileManagement.saveProfile(profile, requireContext())
    }
}

class RewardAdapter(private val rewards: List<Order>) :
    RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    private val redeemPts = getRedeemPoints()

    class RewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.orderTitle)
        val dateTime: TextView = view.findViewById(R.id.orderDateTime)
        val points: TextView = view.findViewById(R.id.points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewards[position]
        holder.title.text = reward.name
        holder.dateTime.text = formatDate(reward.date)

        val totalRedeemPts = (redeemPts[reward.name] ?: 0) * reward.qty
        holder.points.text = "+ ${totalRedeemPts} Pts"
    }

    override fun getItemCount(): Int = rewards.size

    private fun formatDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMMM | hh:mm a", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}


