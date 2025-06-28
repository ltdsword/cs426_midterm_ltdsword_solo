package com.example.codecup

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cuoi.LoginActivity
import com.example.cuoi.Profile
import com.example.cuoi.ProfileManagement
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // button on navigation bar
    private lateinit var homeIcon: ImageButton
    private lateinit var rewardsIcon: ImageButton
    private lateinit var orderIcon: ImageButton

    //user data
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var profile: Profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        homeIcon = findViewById<ImageButton>(R.id.nav_home)
        rewardsIcon = findViewById<ImageButton>(R.id.nav_rewards)
        orderIcon = findViewById<ImageButton>(R.id.nav_order)

        // Check if user is logged in
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // get the user's info
        val data = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username = data.getString("username", null) ?: return
        val profileManagement = ProfileManagement()
        lifecycleScope.launch {
            val profileTemp = profileManagement.getProfile(username)
            if (profileTemp != null) {
                profile = profileTemp
            } else {
                return@launch
            }
            email = profile.email

            setContentView(R.layout.activity_main)

            // Load the default fragment
            loadFragment(HomeFragment())
            setSelectedFragment(HomeFragment(), homeIcon)

            homeIcon.setOnClickListener {
                setSelectedFragment(HomeFragment(), homeIcon)
            }
            rewardsIcon.setOnClickListener {
                setSelectedFragment(RewardFragment(), rewardsIcon)
            }
            orderIcon.setOnClickListener {
                setSelectedFragment(ProfileFragment(), orderIcon)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    private fun setSelectedFragment(fragment: Fragment, selectedButton: ImageButton) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        updateNavSelection(selectedButton)
    }

    private fun updateNavSelection(selected: ImageButton) {
        val buttons = listOf(homeIcon, rewardsIcon, orderIcon)

        buttons.forEach { btn ->
            val isSelected = (btn == selected)

            val fromColor = (btn.backgroundTintList?.defaultColor ?: getColor(R.color.white))
            val toColor = if (isSelected) getColor(R.color.sky) else getColor(R.color.white)

            animateColorTransition(btn, fromColor, toColor)

            btn.isSelected = isSelected // for tint selector if you use one
        }
    }

    private fun animateColorTransition(view: ImageView, fromColor: Int, toColor: Int) {
        val colorAnim = ValueAnimator.ofArgb(fromColor, toColor)
        colorAnim.duration = 300 // duration in ms

        colorAnim.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            view.backgroundTintList = ColorStateList.valueOf(animatedColor)
        }
        colorAnim.start()
    }
}