package com.example.codecup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.cuoi.Profile
import com.example.cuoi.ProfileManagement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsFragment : Fragment() {

    private lateinit var username: String
    private lateinit var profile: Profile

    private val profileManagement = ProfileManagement()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false)
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

    private fun setupUI(view: View) {

    }
}