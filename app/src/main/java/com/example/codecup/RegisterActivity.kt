package com.example.codecup

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.codecup.R

class RegisterActivity : AppCompatActivity() {
    private val profileManagement = ProfileManagement()
    private lateinit var profile: Profile
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameField = findViewById<EditText>(R.id.editTextUsername)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        val confirmPasswordField = findViewById<EditText>(R.id.confirmPassword)


        registerButton.setOnClickListener {
            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (!profileManagement.isLegalUsername(username)) {
                Toast.makeText(this, "Username must not have special characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty() && confirmPassword.isNotEmpty()) {
                // Check if the username is already taken
                if (profileManagement.isUsernameExist(username)) {
                    Toast.makeText(this, "Username is already taken!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else if (password.length > 8) {
                    if (password != confirmPassword) {
                        Toast.makeText(this, "The confirm password is not correct!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    else {
                        val emailVerify = EmailVerify()
                        if (emailVerify.isValidEmail(email)) {
                            // Send the verification
                            Log.d("Register", "Sending verification to email $email")
                            emailVerify.showEmailVerificationDialog(this, email) { isVerified ->
                                if (isVerified) {
                                    // Save user data (SharedPreferences, Database, etc.)
                                    Toast.makeText(
                                        this,
                                        "Registration Successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val hasher = Hasher()
                                    // Save the profile data
                                    val newProfile: Profile = Profile()
                                    newProfile.name = username
                                    newProfile.password = hasher.hash(password)
                                    newProfile.email = email

                                    profileManagement.saveProfile(newProfile)
                                    // Go back to LoginActivity
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish() // Close RegisterActivity
                                }
                            }
                        } else {
                            Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    Toast.makeText(this, "Password must have more than 8 characters!", Toast.LENGTH_SHORT). show()
                }
            } else {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        val loginButton = findViewById<TextView>(R.id.loginButton)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
