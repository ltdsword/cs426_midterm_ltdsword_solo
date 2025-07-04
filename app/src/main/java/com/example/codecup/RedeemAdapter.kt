package com.example.codecup

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

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