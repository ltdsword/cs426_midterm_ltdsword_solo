package com.example.codecup

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OngoingFragment : Fragment() {

    private lateinit var orderAdapter: OrderAdapter
    private lateinit var profile: Profile

    private val profileManagement = ProfileManagement()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_order_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profile = profileManagement.getProfileFromLocal(requireContext())

        orderAdapter = OrderAdapter(profile.ongoing.hist.toMutableList(), true) { position ->
            // Move to history (logic to delete the ongoing orders)
            val ongoing = profile.ongoing.hist.toMutableList()
            val history = profile.history.hist.toMutableList()

            val item = ongoing[position]
            ongoing.removeAt(position)
            history.add(item)

            profile.ongoing.hist = ongoing
            profile.history.hist = history

            profileManagement.saveProfile(profile, requireContext())

            orderAdapter.removeAt(position)
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.orderRecyclerView)
        recyclerView.adapter = orderAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        attachSwipeToComplete(recyclerView, orderAdapter)
    }

    private fun attachSwipeToComplete(recyclerView: RecyclerView, adapter: OrderAdapter) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val foreground = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                foreground.translationX = dX.coerceAtMost(foreground.width * 0.3f)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Do not mark complete on swipe – wait for button tap
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.3f
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

}
