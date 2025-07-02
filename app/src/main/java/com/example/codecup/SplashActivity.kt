package com.example.codecup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Use a Handler to delay the transition to the main activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Create an Intent to start the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Finish the splash activity so the user can't go back to it
            finish()
        }, 3000) // 3000 milliseconds = 3 seconds
    }
}