package com.example.codecup

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class OrderFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavSelection(view)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

        val adapter = OrderPagerAdapter(this)
        viewPager.adapter = adapter

        // Attach TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab_text, null)
            val textView = tabView.findViewById<TextView>(R.id.tabText)
            textView.text = if (position == 0) "On going" else "History"
            tab.customView = tabView
        }.attach()
    }

    private fun updateNavSelection(view: View) {
        requireActivity().findViewById<MaterialCardView>(R.id.bottom_navi_bar)?.visibility = View.VISIBLE
        val homeIcon = requireActivity().findViewById<ImageButton>(R.id.nav_home)
        val rewardsIcon = requireActivity().findViewById<ImageButton>(R.id.nav_rewards)
        val orderIcon = requireActivity().findViewById<ImageButton>(R.id.nav_order)
        val buttons = listOf(homeIcon, rewardsIcon, orderIcon)

        buttons.forEach { btn ->
            val isSelected = (btn == orderIcon)

            val fromColor = (btn.backgroundTintList?.defaultColor ?: getColor(requireContext(), R.color.white))
            val toColor = if (isSelected) getColor(requireContext() ,R.color.sky) else getColor(requireContext(), R.color.white)

            animateColorTransition(btn, fromColor, toColor)

            btn.isSelected = isSelected // for tint selector if you use one
        }
    }

    private fun animateColorTransition(view: ImageView, fromColor: Int, toColor: Int) {
        val colorAnim = ValueAnimator.ofArgb(fromColor, toColor)
        colorAnim.duration = 300 // duration in ms

        colorAnim.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            view.backgroundTintList = ColorStateList.valueOf(animatedColor)
        }
        colorAnim.start()
    }
}


class OrderPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OngoingFragment()
            1 -> HistoryFragment()
            else -> throw IllegalArgumentException("Invalid page index")
        }
    }
}



class OrderAdapter(
    private val orders: MutableList<Order>,
    private val isOngoing: Boolean,
    val onSwipeComplete: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTime: TextView = itemView.findViewById(R.id.orderDateTime)
        val title: TextView = itemView.findViewById(R.id.orderTitle)
        val price: TextView = itemView.findViewById(R.id.orderPrice)
        val address: TextView = itemView.findViewById(R.id.orderAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.dateTime.text = reformat(order.date)
        holder.title.text = order.name
        holder.price.text = "$%.2f".format(order.price)
        holder.address.text = order.address

        holder.itemView.findViewById<ImageButton>(R.id.completeButton).setOnClickListener {
            onSwipeComplete?.invoke(holder.adapterPosition)
        }
    }

    private fun reformat(date: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMMM | hh:mm a", Locale.getDefault())
        return sdf.format(date.toDate())
    }

    fun removeAt(position: Int) {
        orders.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getOrderAt(position: Int) = orders[position]
}
