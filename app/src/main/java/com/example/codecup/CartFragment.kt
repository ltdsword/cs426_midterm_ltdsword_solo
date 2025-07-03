package com.example.codecup

import android.app.AlertDialog
import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
class CartFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var adapter: CartAdapter

    private lateinit var profile: Profile
    private val profileManagement = ProfileManagement()

    private var address: String = ""

    private var swipedViewHolder: RecyclerView.ViewHolder? = null

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


        adapter = CartAdapter(cartViewModel) // Simplified constructor
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cartViewModel.cartItems.observe(viewLifecycleOwner) {
            // ListAdapter's submitList will handle all the updates and animations for you!
            adapter.submitList(it)
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

        attachSwipeToDelete(recyclerView)

        // Add a scroll listener to automatically close any swiped item when the user scrolls.
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // User is scrolling, close any open item.
                    closeSwipedItem()
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        // save the profile data
        profileManagement.saveProfile(profile, requireContext())
    }

    private fun closeSwipedItem() {
        swipedViewHolder?.itemView?.findViewById<View>(R.id.foregroundLayout)
            ?.animate()
            ?.translationX(0f)
            ?.setDuration(200)
            ?.start()
        swipedViewHolder = null
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
        var totalPrice = 0.00
        for (o in cartItems) {
            // save order to history
            val ltypts = getRedeemPoints(o.price)
            profile.ongoing.addObject(o.name, o.price*o.qty, o.qty, o.qty*ltypts, address)
            profile.loyaltyPts += o.qty
            profile.loyaltyPts %= 8

            profile.points += ltypts * o.qty
            totalPrice += o.price*o.qty
        }

        // send email to user
        val emailVerify = EmailVerify()
        emailVerify.sendOrderSuccessEmail(profile.email, cartItems, totalPrice, requireContext())

        // save profile
        profileManagement.saveProfile(profile, requireContext())

        // empty the cart
        cartViewModel.cartItems.value?.clear()
        cartViewModel.saveCartToPrefs()
    }

    private fun attachSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Close any other open item if we start swiping a new one.
                    if (isCurrentlyActive && swipedViewHolder != null && swipedViewHolder != viewHolder) {
                        closeSwipedItem()
                    }

                    val foregroundView = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                    val deleteButton = viewHolder.itemView.findViewById<View>(R.id.deleteButton)
                    val buttonWidth = deleteButton.width.toFloat()

                    // Manually set translation on the foreground view only.
                    // The new dX is calculated based on the initial position to allow swiping from an open state.
                    val newDx = foregroundView.translationX + dX
                    foregroundView.translationX = newDx.coerceIn(-buttonWidth, 0f)

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                val deleteButton = viewHolder.itemView.findViewById<View>(R.id.deleteButton)
                val buttonWidth = deleteButton.width.toFloat()

                if (swipedViewHolder == viewHolder) {
                    // If it's open, you must swipe RIGHT past the threshold to close it.
                    val closeThreshold = buttonWidth * 0.7f
                    // We check if the translation has moved far enough to the right (is greater than -closeThreshold)
                    if (foregroundView.translationX > -closeThreshold) {
                        closeSwipedItem() // Animate it closed and clear state.
                    } else {
                        // Not swiped far enough right, so snap it back to the fully open state.
                        foregroundView.animate().translationX(-buttonWidth).setDuration(200).start()
                    }
                } else { // This item was not the one that was open.
                    val openThreshold = buttonWidth * 0.3f
                    if (foregroundView.translationX < -openThreshold) {
                        foregroundView.animate().translationX(-buttonWidth).setDuration(200).start()
                        swipedViewHolder = viewHolder
                    } else {
                        // Not swiped far enough left, so snap it fully closed.
                        foregroundView.translationX = 0f
                    }
                }
            }

            // Disable the default dismissal behavior.
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return Float.MAX_VALUE
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }
}


class CartAdapter(
    private val cartViewModel: CartViewModel
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

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

