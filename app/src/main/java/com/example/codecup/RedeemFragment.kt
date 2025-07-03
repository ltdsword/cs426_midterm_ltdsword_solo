package com.example.codecup

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

    override fun onStop() {
        super.onStop()
        // save the profile data
    }
}

class RedeemAdapter(private val items: List<RedeemItem>) :
    RecyclerView.Adapter<RedeemAdapter.RedeemViewHolder>() {

    private lateinit var profile: Profile

    private val profileManagement = ProfileManagement()

    class RedeemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coffeeName: TextView = view.findViewById(R.id.coffeeName)
        val orderDateTime: TextView = view.findViewById(R.id.orderDateTime)
        val redeemPts: Button = view.findViewById(R.id.redeemPts)
        val image: ImageView = view.findViewById(R.id.imagePic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RedeemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_redeem, parent, false)
        return RedeemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RedeemViewHolder, position: Int) {
        val item = items[position]
        holder.coffeeName.text = item.name
        holder.orderDateTime.text = "Valid until ${item.validUntil}"
        holder.redeemPts.text = "${item.requiredPoints} pts"
        holder.image.setImageResource(item.imageResId)

        profile = profileManagement.getProfileFromLocal(holder.itemView.context)

        holder.redeemPts.setOnClickListener {
            if (profile.points >= item.requiredPoints) {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Confirm Redemption")
                    .setMessage("Redeem ${item.name} for ${item.requiredPoints} points?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Proceed with redemption
                        Log.d("RedeemAdapter", "Redeeming ${item.name}, Init points: ${profile.points}")
                        profile.points -= item.requiredPoints
                        profileManagement.saveProfile(profile, holder.itemView.context)
                        Log.d("RedeemAdapter", "Final points: ${profile.points}")
                        Toast.makeText(holder.itemView.context, "Redeemed ${item.name}!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(holder.itemView.context, "Not enough points", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = items.size
}











