package com.example.codecup

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cuoi.Coffee
import com.example.cuoi.Profile
import com.example.cuoi.ProfileManagement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private lateinit var coffeeRecyclerView: RecyclerView

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
        lifecycleScope.launch(Dispatchers.IO) {
            val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            username = data.getString("username", null) ?: return@launch
            val profileTemp = profileManagement.getProfile(username)
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

        cartImage.setOnClickListener {
            Toast.makeText(requireContext(), "Cart clicked", Toast.LENGTH_SHORT).show()
        }

        profileImage.setOnClickListener {
            Toast.makeText(requireContext(), "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        coffeeRecyclerView = view.findViewById(R.id.coffeeRecyclerView)

        val coffeeList = getCoffeeList()

        val adapter = CoffeeAdapter(coffeeList) { coffee ->
            Toast.makeText(requireContext(), "Clicked: ${coffee.name}", Toast.LENGTH_SHORT).show()
            showCoffeeDetails(coffee)
        }

        coffeeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

// spacing between columns and rows (e.g. 8dp)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.grid_spacing) // define as 8dp
        coffeeRecyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingPx, true))

        coffeeRecyclerView.adapter = adapter
    }

    private fun showCoffeeDetails(coffee: Coffee) {
        loadFragment()
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