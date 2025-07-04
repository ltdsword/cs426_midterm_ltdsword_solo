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
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
class CartFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var adapter: CartAdapter

    private lateinit var profile: Profile
    private val profileManagement = ProfileManagement()

    private var address: String = ""

    // for the swipe activity
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

        profile = profileManagement.getProfileFromLocal(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)
        val totalPriceText = view.findViewById<TextView>(R.id.totalPrice)
        val checkoutButton = view.findViewById<Button>(R.id.checkoutButton)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)


        adapter = CartAdapter(cartViewModel) { selectedCoffee ->
            navigateToDetails(selectedCoffee)
        }

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

    private fun navigateToDetails(coffee: Coffee) {
        val fragment = DetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DetailsFragment.ARG_COFFEE, coffee)
                putBoolean(DetailsFragment.ARG_IS_MODIFYING, true)
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
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
            val rdpts = getRedeemPoints(o.price * o.qty)
            profile.ongoing.addObject(o.name, o.price*o.qty, o.qty, rdpts, address)
            profile.loyaltyPts += o.qty
            profile.loyaltyPts %= 8

            profile.points += rdpts
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
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (isCurrentlyActive && swipedViewHolder != null && swipedViewHolder != viewHolder) {
                        closeSwipedItem()
                    }

                    val foreground = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                    val deleteButton = viewHolder.itemView.findViewById<View>(R.id.deleteButton)
                    val buttonWidth = deleteButton.width.toFloat()

                    deleteButton.isEnabled = false
                    deleteButton.isClickable = false

                    val newDx = foreground.translationX + dX
                    foreground.translationX = newDx.coerceIn(-buttonWidth, 0f)

                    if (foreground.translationX <= -buttonWidth) {
                        deleteButton.isEnabled = true
                        deleteButton.isClickable = true
                    } else {
                        deleteButton.isEnabled = false
                        deleteButton.isClickable = false
                    }
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val foreground = viewHolder.itemView.findViewById<View>(R.id.foregroundLayout)
                val deleteButton = viewHolder.itemView.findViewById<View>(R.id.deleteButton)
                val buttonWidth = deleteButton.width.toFloat()

                deleteButton.isEnabled = false
                deleteButton.isClickable = false

                if (swipedViewHolder == viewHolder) {
                    val closeThreshold = buttonWidth * 0.7f
                    if (foreground.translationX > -closeThreshold) {
                        closeSwipedItem()
                    } else {
                        foreground.animate().translationX(-buttonWidth).setDuration(200).start()
                        deleteButton.isEnabled = true
                    }
                } else {
                    val openThreshold = buttonWidth * 0.3f
                    if (foreground.translationX < -openThreshold) {
                        foreground.animate().translationX(-buttonWidth).setDuration(200).start()
                        deleteButton.isEnabled = true
                        swipedViewHolder = viewHolder
                    } else {
                        foreground.translationX = 0f
                        deleteButton.isEnabled = false
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


