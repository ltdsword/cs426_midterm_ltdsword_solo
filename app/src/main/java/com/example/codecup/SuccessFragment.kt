package com.example.codecup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class SuccessFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val trackOrder = view.findViewById<Button>(R.id.trackOrder)
        trackOrder.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, // enter
                    R.anim.slide_out_left, // exit
                    R.anim.slide_in_left,  // popEnter
                    R.anim.slide_out_right // popExit
                )
                .replace(R.id.fragment_container, OrderFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}