package com.example.codecup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class RewardAdapter(private val rewards: List<Order>) :
    RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

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

        val totalRedeemPts = getRedeemPoints(reward.price)
        holder.points.text = "+ ${totalRedeemPts} Pts"
    }

    override fun getItemCount(): Int = rewards.size

    private fun formatDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMMM | hh:mm a", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}