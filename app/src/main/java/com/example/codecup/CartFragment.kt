package com.example.codecup

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import java.util.Date

class CartFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var adapter: CartAdapter

    private lateinit var profile: Profile
    private val profileManagement = ProfileManagement()

    private var address: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().findViewById<MaterialCardView>(R.id.bottom_navi_bar)?.visibility = View.GONE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })

        val recyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)
        val totalPriceText = view.findViewById<TextView>(R.id.totalPrice)
        val checkoutButton = view.findViewById<Button>(R.id.checkoutButton)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)


        adapter = CartAdapter(mutableListOf(), cartViewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cartViewModel.cartItems.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            totalPriceText.text = String.format("$%.2f", total)
        }

        backButton.setOnClickListener {
            navigateBackToHome()
        }

        profile = profileManagement.getProfileFromLocal(requireContext())

        // check out: save data....
        checkoutButton.setOnClickListener {
            checkAddress()
        }

        attachSwipeToDelete(recyclerView, adapter)
    }

    // pop all the components in the stack, return to HomeFragment
    private fun navigateBackToHome() {
        // Clear the entire back stack and go to HomeFragment
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    private fun checkAddress() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.check_address, null)
        val addressText = dialogView.findViewById<TextView>(R.id.address)
        val clickAddress = dialogView.findViewById<TextView>(R.id.clickAddress)

        if (profile.address == "") {
            clickAddress.visibility = View.GONE
        }
        else {
            clickAddress.visibility = View.VISIBLE
        }

        clickAddress.setOnClickListener {
            addressText.text = profile.address
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Confirm") { dialogInterface, _ ->
                // Handle confirm action here
                address = addressText.text.toString()
                saveOrder()
                dialogInterface.dismiss()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right, // enter
                        R.anim.slide_out_left, // exit
                        R.anim.slide_in_left,  // popEnter
                        R.anim.slide_out_right // popExit
                    )
                    .replace(R.id.fragment_container, SuccessFragment())
                    .addToBackStack(null)
                    .commit()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()


        dialog.show()
    }

    private fun saveOrder() {
        val cartItems = cartViewModel.cartItems.value ?: return
        for (o in cartItems) {
            // save order to history
            val ltypts = getRedeemPoints()[o.name] ?: 0
            profile.ongoing.addObject(o.name, o.price*o.qty, o.qty, o.qty*ltypts, address)
            profile.loyaltyPts += o.qty
            profile.loyaltyPts %= 8
        }

        profileManagement.saveProfile(profile, requireContext())

        Log.d("Order", profile.ongoing.toString())
    }

    private fun attachSwipeToDelete(recyclerView: RecyclerView, adapter: CartAdapter) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val foreground = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                foreground.translationX = dX.coerceAtLeast(-foreground.width * 0.3f) // Limit max swipe
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Don't remove on swipe; reset it
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.3f
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}


class CartAdapter(private var items: List<Coffee>, private val cartViewModel: CartViewModel) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun updateData(newItems: List<Coffee>) {
        items = newItems
        notifyDataSetChanged()
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coffeeName = itemView.findViewById<TextView>(R.id.name)
        val coffeeDetails = itemView.findViewById<TextView>(R.id.details)
        val coffeeQty = itemView.findViewById<TextView>(R.id.qty)
        val coffeePrice = itemView.findViewById<TextView>(R.id.price)
        val coffeeImage = itemView.findViewById<ImageView>(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.coffeeName.text = item.name
        holder.coffeeQty.text = "x ${item.qty}"
        holder.coffeePrice.text = String.format("$%.2f", item.price * item.qty)
        holder.coffeeDetails.text =
            "${if (item.single) "single" else "double"} | ${if (item.hot) "hot" else "iced"} | ${getSizeLabel(item.size)} | ${getIceLabel(item.ice)}"
        holder.coffeeImage.setImageResource(item.imageResId)

        holder.itemView.findViewById<ImageButton>(R.id.deleteButton).setOnClickListener {
            items = listOf(items.toMutableList().removeAt(position))
            notifyItemRemoved(position)
            cartViewModel.removeItem(position)  // or notify ViewModel
        }
    }

    override fun getItemCount() = items.size

    private fun getSizeLabel(size: Int) = when (size) {
        0 -> "small"
        1 -> "medium"
        else -> "large"
    }

    private fun getIceLabel(ice: Int) = when (ice) {
        0 -> "little ice"
        1 -> "medium ice"
        2 -> "full ice"
        else -> "unknown"
    }
}

