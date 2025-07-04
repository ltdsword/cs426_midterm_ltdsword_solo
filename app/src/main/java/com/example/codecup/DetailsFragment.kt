package com.example.codecup

import android.app.AlertDialog
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
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView


class DetailsFragment : Fragment() {

    companion object {
        const val ARG_COFFEE = "arg_coffee"
        const val ARG_IS_MODIFYING = "arg_is_modifying"
    }

    // coffee object
    private lateinit var coffee: Coffee
    private lateinit var oldCoffee: Coffee
    private val cartViewModel: CartViewModel by activityViewModels()

    private lateinit var profile: Profile
    private val profileManagement = ProfileManagement()

    // is modifying the cart or not
    private var isModifying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coffee = arguments?.getParcelable(ARG_COFFEE)
            ?: throw IllegalArgumentException("Coffee must not be null")

        oldCoffee = coffee.copy()

        // we assign the coffee obj with the params
        isModifying = arguments?.getBoolean(ARG_IS_MODIFYING, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // back press logic
        // Handle device/system back button
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardConfirmationDialog(this)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            showDiscardConfirmationDialog(callback)
        }

        setupImageUI(view)

        profile = profileManagement.getProfileFromLocal(requireContext())

        setupUI(view)
    }

    private lateinit var qtyText: TextView
    private lateinit var addButton: TextView
    private lateinit var subtractButton: TextView
    private lateinit var coffeeName: TextView

    private fun setupImageUI(view: View) {
        // Hide bottom navigation
        requireActivity().findViewById<MaterialCardView>(R.id.bottom_navi_bar)?.visibility = View.GONE
        // Set image
        val coffeeImage = view.findViewById<ImageView>(R.id.coffeeImage)
        coffeeImage.setImageResource(coffee.imageResId)

        // Set name
        coffeeName = view.findViewById<TextView>(R.id.coffeeName)
        coffeeName.text = coffee.name

        // Quantity
        qtyText = view.findViewById(R.id.qty)
        addButton = view.findViewById(R.id.add)
        subtractButton = view.findViewById(R.id.subtract)
        qtyText.text = coffee.qty.toString()

        val cart = view.findViewById<ImageView>(R.id.cartButton)
        cart.setOnClickListener {
            CartBottomSheetFragment().show(parentFragmentManager, "CartBottomSheet")
        }
    }

    private fun setupUI(view: View) {
        addButton.setOnClickListener {
            coffee.qty++
            qtyText.text = coffee.qty.toString()
            updatePrice(view)
        }

        subtractButton.setOnClickListener {
            if (coffee.qty > 1) {
                coffee.qty--
                qtyText.text = coffee.qty.toString()
                updatePrice(view)
            }
        }

        // Shot: Single/Double
        val singleButton = view.findViewById<Button>(R.id.singleSelect)
        val doubleButton = view.findViewById<Button>(R.id.doubleSelect)

        fun updateShotUI() {
            if (coffee.single) {
                singleButton.setBackgroundResource(R.drawable.round_border_black)
                doubleButton.setBackgroundResource(R.drawable.round_border)
            } else {
                doubleButton.setBackgroundResource(R.drawable.round_border_black)
                singleButton.setBackgroundResource(R.drawable.round_border)
            }
        }

        singleButton.setOnClickListener {
            coffee.single = true
            updateShotUI()
        }

        doubleButton.setOnClickListener {
            coffee.single = false
            updateShotUI()
        }

        updateShotUI()

        // Hot/Cold
        val hotImage = view.findViewById<ImageView>(R.id.hot)
        val coldImage = view.findViewById<ImageView>(R.id.cold)

        fun updateHotColdUI() {
            hotImage.alpha = if (coffee.hot) 1f else 0.3f
            coldImage.alpha = if (coffee.hot) 0.3f else 1f
        }

        updateHotColdUI()

        // Size selection (0 = small, 1 = med, 2 = big)
        val sizeSmall = view.findViewById<ImageView>(R.id.small)
        val sizeMed = view.findViewById<ImageView>(R.id.med)
        val sizeBig = view.findViewById<ImageView>(R.id.big)

        fun updateSizeUI() {
            sizeSmall.alpha = if (coffee.size == 0) 1f else 0.3f
            sizeMed.alpha = if (coffee.size == 1) 1f else 0.3f
            sizeBig.alpha = if (coffee.size == 2) 1f else 0.3f

            updatePrice(view)
        }

        sizeSmall.setOnClickListener {
            coffee.size = 0
            updateSizeUI()
        }

        sizeMed.setOnClickListener {
            coffee.size = 1
            updateSizeUI()
        }

        sizeBig.setOnClickListener {
            coffee.size = 2
            updateSizeUI()
        }

        updateSizeUI()

        // Ice selection (0, 1, 2)
        val ice0 = view.findViewById<ImageView>(R.id.little)
        val ice1 = view.findViewById<ImageView>(R.id.medium)
        val ice2 = view.findViewById<ImageView>(R.id.full)

        hotImage.setOnClickListener {
            // cannot choose ice
            coffee.hot = true
            ice0.isEnabled = false
            ice1.isEnabled = false
            ice2.isEnabled = false
            coffee.ice = -1
            ice0.alpha = 0.3f
            ice1.alpha = 0.3f
            ice2.alpha = 0.3f
            updateHotColdUI()
        }

        coldImage.setOnClickListener {
            coffee.hot = false
            ice0.isEnabled = true
            ice1.isEnabled = true
            ice2.isEnabled = true
            coffee.ice = 0
            ice0.alpha = 1.0f
            ice1.alpha = 0.3f
            ice2.alpha = 0.3f
            updateHotColdUI()
        }

        fun updateIceUI() {
            if (coffee.hot) {
                ice0.alpha = 0.3f
                ice1.alpha = 0.3f
                ice2.alpha = 0.3f
                coffee.ice = -1
            }
            else {
                ice0.alpha = if (coffee.ice == 0) 1f else 0.3f
                ice1.alpha = if (coffee.ice == 1) 1f else 0.3f
                ice2.alpha = if (coffee.ice == 2) 1f else 0.3f
            }
        }

        ice0.setOnClickListener {
            coffee.ice = 0
            updateIceUI()
        }

        ice1.setOnClickListener {
            coffee.ice = 1
            updateIceUI()
        }

        ice2.setOnClickListener {
            coffee.ice = 2
            updateIceUI()
        }

        updateIceUI()
        updatePrice(view)

        // Add to Cart
        val addToCart = view.findViewById<Button>(R.id.addToCartButton)
        val titleTextView = view.findViewById<TextView>(R.id.titleText)
        if (isModifying) {
            addToCart.text = "Modify this coffee"
            titleTextView.text = "Modify"
        }
        addToCart.setOnClickListener {
            if (isModifying) {
                cartViewModel.updateItem(oldCoffee, coffee)
                cartViewModel.notifyChange()
            } else {
                cartViewModel.addItem(coffee)
            }
            // switch to CartFragment
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, // enter
                    R.anim.slide_out_left, // exit
                    R.anim.slide_in_left,  // popEnter
                    R.anim.slide_out_right // popExit
                )
                .replace(R.id.fragment_container, CartFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updatePrice(view: View) {
        if (coffee.size == 1) {
            coffee.price = coffee.priceMed
        }
        else if (coffee.size == 2) {
            coffee.price = coffee.priceBig
        }
        else {
            coffee.price = coffee.priceSmall
        }
        val totalPrice = coffee.price * coffee.qty
        val priceText = view.findViewById<TextView>(R.id.totalPrice)
        priceText.text = "$%.2f".format(totalPrice)
    }

    private fun showDiscardConfirmationDialog(callback: OnBackPressedCallback) {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard Changes?")
            .setMessage("Do you want to discard changes and go back?")
            .setPositiveButton("Yes") { _, _ ->
                // Prevent infinite loop
                callback.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onStop() {
        super.onStop()
        // save the profile data
        profileManagement.saveProfile(profile, requireContext())
    }

}


