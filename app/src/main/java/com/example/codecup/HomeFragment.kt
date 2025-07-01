package com.example.codecup

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson


// use a Shared View model to manipulate a cart in fragments
// they can modify the cart to display in CartFragment
class CartViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<MutableList<Coffee>>(mutableListOf())
    val cartItems: LiveData<MutableList<Coffee>> = _cartItems

    val totalPrice: LiveData<Double> = cartItems.map { cart ->
        cart.sumOf { coffee -> coffee.price * coffee.qty }
    }

    fun addItem(coffee: Coffee) {
        _cartItems.value?.let { list ->
            list.add(coffee)
            _cartItems.value = list.toMutableList()
        }
    }

    fun removeItem(position: Int) {
        _cartItems.value?.let {
            it.removeAt(position)
            _cartItems.value = it.toMutableList()
        }
    }
}


class HomeFragment : Fragment() {

    private lateinit var coffeeRecyclerView: RecyclerView
    private val cartViewModel: CartViewModel by activityViewModels()

    private lateinit var username: String
    private lateinit var profile: Profile

    private val profileManagement = ProfileManagement()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // show the bottom navigation bar
        requireActivity().findViewById<MaterialCardView>(R.id.bottom_navi_bar)?.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            username = data.getString("username", null) ?: return@launch
            val json = data.getString("profile", null)
            val profileTemp = if (json != null) {
                Gson().fromJson(json, Profile::class.java)
            } else {
                null
            }
            withContext(Dispatchers.Main) {
                if (profileTemp != null) {
                    profile = profileTemp
                    setupUI(view)
                } else {
                    return@withContext
                }
            }
        }
    }

    private fun setupUI(view:View) {
        val cartImage = view.findViewById<ImageView>(R.id.cart)
        val profileImage = view.findViewById<ImageView>(R.id.profile)
        val nameText = view.findViewById<TextView>(R.id.nameText)

        val ltyPts = view.findViewById<TextView>(R.id.ltyid)
        val cups = listOf(R.id.c1, R.id.c2, R.id.c3, R.id.c4, R.id.c5, R.id.c6, R.id.c7, R.id.c8)
        val cupImages = cups.map { view.findViewById<ImageView>(it) }

        val pts = profile.loyaltyPts
        ltyPts.text = "$pts / 8"

        for (i in 0 until 8) {
            if (i < pts) {
                cupImages[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.brown), PorterDuff.Mode.SRC_IN)
            }
            else {
                cupImages[i].alpha = 0.4f
            }
        }

        nameText.text = profile.name

        cartImage.setOnClickListener {
            // Toast.makeText(requireContext(), "Cart clicked", Toast.LENGTH_SHORT).show()
            // replace the fragment inside this container view with ID fragment_container with the new one
            parentFragmentManager.beginTransaction()
                // this is for smooth transition
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

        profileImage.setOnClickListener {
            // Toast.makeText(requireContext(), "Profile clicked", Toast.LENGTH_SHORT).show()
            // replace the fragment inside this container view with ID fragment_container with the new one
            parentFragmentManager.beginTransaction()
                // this is for smooth transition
                .setCustomAnimations(
                    R.anim.slide_in_right, // enter
                    R.anim.slide_out_left, // exit
                    R.anim.slide_in_left,  // popEnter
                    R.anim.slide_out_right // popExit
                )
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        coffeeRecyclerView = view.findViewById(R.id.coffeeRecyclerView)

        val coffeeList = getCoffeeList()

        val adapter = CoffeeAdapter(coffeeList) { coffee ->
            // Toast.makeText(requireContext(), "Clicked: ${coffee.name}", Toast.LENGTH_SHORT).show()
            showCoffeeDetails(coffee)
        }

        coffeeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

// spacing between columns and rows (e.g. 8dp)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing) // define as 8dp
        coffeeRecyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingPx, true))

        coffeeRecyclerView.adapter = adapter
    }

    private fun showCoffeeDetails(coffee: Coffee) {
        // create the fragment with that coffee as argument
        val fragment = DetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DetailsFragment.ARG_COFFEE, coffee)
            }
        }

        // replace the fragment inside this container view with ID fragment_container with the new one
        parentFragmentManager.beginTransaction()
            // this is for smooth transition
            .setCustomAnimations(
                R.anim.slide_in_right, // enter
                R.anim.slide_out_left, // exit
                R.anim.slide_in_left,  // popEnter
                R.anim.slide_out_right // popExit
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

class CoffeeAdapter(
    private val items: List<Coffee>,
    private val onItemClick: (Coffee) -> Unit
) : RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder>() {

    inner class CoffeeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.coffeeImage)
        val name: TextView = view.findViewById(R.id.coffeeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coffee, parent, false)
        return CoffeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageResource(item.imageResId)
        holder.name.text = item.name

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}