package com.example.codecup

import android.graphics.Canvas
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

    private var swipedViewHolder: RecyclerView.ViewHolder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_order_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profile = profileManagement.getProfileFromLocal(requireContext())

        orderAdapter = OrderAdapter(profile.ongoing.hist.toMutableList(), true,
            onComplete = { position ->
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
        )
        val recyclerView = view.findViewById<RecyclerView>(R.id.orderRecyclerView)
        recyclerView.adapter = orderAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        attachSwipeToComplete(recyclerView)

        // Add a scroll listener to close any open item when scrolling. Great for UX.
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    closeSwipedItem()
                }
            }
        })
    }

    private fun closeSwipedItem() {
        swipedViewHolder?.itemView?.findViewById<View>(R.id.foregroundLayout)
            ?.animate()
            ?.translationX(0f)
            ?.setDuration(200)
            ?.start()
        swipedViewHolder = null
    }

    private fun attachSwipeToComplete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { /* Not used */ }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (isCurrentlyActive && swipedViewHolder != null && swipedViewHolder != viewHolder) {
                        closeSwipedItem()
                    }

                    val foregroundView = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                    val completeButton = viewHolder.itemView.findViewById<View>(R.id.completeButton)
                    val buttonWidth = completeButton.width.toFloat()

                    // Manually set translation, clamping between 0 (closed) and buttonWidth (fully open)
                    val newDx = foregroundView.translationX + dX
                    foregroundView.translationX = newDx.coerceIn(0f, buttonWidth) // Swiping RIGHT now

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                val completeButton = viewHolder.itemView.findViewById<View>(R.id.completeButton)
                val buttonWidth = completeButton.width.toFloat()

                // Logic for an item that was ALREADY OPEN
                if (swipedViewHolder == viewHolder) {
                    val closeThreshold = buttonWidth * 0.7f
                    // If swiped LEFT enough to close it...
                    if (foregroundView.translationX < closeThreshold) {
                        closeSwipedItem()
                    } else {
                        // Not swiped far enough, snap it back to fully open.
                        foregroundView.animate().translationX(buttonWidth).setDuration(200).start()
                    }
                } else { // Logic for an item that was CLOSED
                    val openThreshold = buttonWidth * 0.3f
                    // If swiped RIGHT enough to open it...
                    if (foregroundView.translationX > openThreshold) {
                        foregroundView.animate().translationX(buttonWidth).setDuration(200).start()
                        swipedViewHolder = viewHolder
                    } else {
                        // Not swiped far enough, snap it fully closed.
                        foregroundView.translationX = 0f
                    }
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return Float.MAX_VALUE
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }
}
