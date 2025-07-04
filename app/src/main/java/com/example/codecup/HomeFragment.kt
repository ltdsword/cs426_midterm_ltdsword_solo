package com.example.codecup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale


class HomeFragment : Fragment() {

    private lateinit var coffeeRecyclerView: RecyclerView
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

        profile = profileManagement.getProfileFromLocal(requireContext())

        setupUI(view)
    }

    private fun setupUI(view:View) {
        val cartImage = view.findViewById<ImageView>(R.id.cart)
        val profileImage = view.findViewById<ImageView>(R.id.profile)
        val nameText = view.findViewById<TextView>(R.id.nameText)
        val greetingText = view.findViewById<TextView>(R.id.greetings)

        val ltyPts = view.findViewById<TextView>(R.id.ltyid)
        val cups = listOf(R.id.c1, R.id.c2, R.id.c3, R.id.c4, R.id.c5, R.id.c6, R.id.c7, R.id.c8)
        val cupImages = cups.map { view.findViewById<ImageView>(it) }

        val pts = profile.loyaltyPts
        ltyPts.text = "$pts / 8"

        for (i in 0 until 8) {
            if (i < pts) {
                cupImages[i].alpha = 1.0f
            }
            else {
                cupImages[i].alpha = 0.4f
            }
        }

        val curHour = formatHour(Timestamp.now()).toInt()
        var txt = "Good "
        if (curHour in 1..11) {
            txt += "morning"
        }
        else if (curHour in 12..17) {
            txt += "afternoon"
        }
        else {
            txt += "evening"
        }
        greetingText.text = txt

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

    private fun formatHour(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("HH", Locale.getDefault()) // 12-hour format
        return sdf.format(timestamp.toDate())
    }

    override fun onStop() {
        super.onStop()
        // save the profile data
        profileManagement.saveProfile(profile, requireContext())
    }
}