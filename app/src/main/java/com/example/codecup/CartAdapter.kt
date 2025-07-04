package com.example.codecup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val cartViewModel: CartViewModel,
    private val onItemClick: ((Coffee) -> Unit)
) : ListAdapter<Coffee, CartAdapter.CartViewHolder>(CoffeeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, cartViewModel)

        holder.itemView.findViewById<ImageButton>(R.id.deleteButton).setOnClickListener {
            cartViewModel.removeItem(item)
            notifyItemRemoved(position)
        }
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coffeeName = itemView.findViewById<TextView>(R.id.name)
        private val coffeeDetails = itemView.findViewById<TextView>(R.id.details)
        private val coffeeQty = itemView.findViewById<TextView>(R.id.qty)
        private val coffeePrice = itemView.findViewById<TextView>(R.id.price)
        private val coffeeImage = itemView.findViewById<ImageView>(R.id.image)
        private val deleteButton = itemView.findViewById<ImageButton>(R.id.deleteButton)
        private val foregroundLayout = itemView.findViewById<View>(R.id.foregroundLayout)

        fun bind(item: Coffee, cartViewModel: CartViewModel) {
            coffeeName.text = item.name
            coffeeQty.text = "x ${item.qty}"
            coffeePrice.text = String.format("$%.2f", item.price * item.qty)
            coffeeDetails.text =
                "${if (item.single) "single" else "double"} | ${if (item.hot) "hot" else "iced"} | ${getSizeLabel(item.size)} | ${getIceLabel(item.ice)}"
            coffeeImage.setImageResource(item.imageResId)

            deleteButton.setOnClickListener {
                // Animate the view back to hide the button before removing
                foregroundLayout.animate()
                    .translationX(0f)
                    .setDuration(200)
                    .start()
            }

            // allow to click the item
            foregroundLayout.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

fun getSizeLabel(size: Int) = when (size) {
    0 -> "small"; 1 -> "medium"; else -> "large"
}
fun getIceLabel(ice: Int) = when (ice) {
    0 -> "little ice"; 1 -> "medium ice"; 2 -> "full ice"; -1 -> "no ice"; else -> "unknown"
}

// DiffUtil calculates the difference between two lists and enables animations
class CoffeeDiffCallback : DiffUtil.ItemCallback<Coffee>() {
    override fun areItemsTheSame(oldItem: Coffee, newItem: Coffee): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Coffee, newItem: Coffee): Boolean {
        return oldItem == newItem
    }
}