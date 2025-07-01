package com.example.codecup
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.codecup.R
import com.google.firebase.Timestamp
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class EmailVerify {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun generateVerificationCode(): String {
        val random = (100000..999999).random()  // Generates a 6-digit random number
        return random.toString()
    }

    private fun sendVerificationEmail(toEmail: String, verificationCode: String, context: Context) {
        val apiKey = (context.applicationContext as MyApplication).sharedPreferences
            .getString("API_KEY", null) ?: return

        Log.d("Email", "Api Key: ${apiKey.substring(0, 10)}")

        val fromEmail = "ltdsword12@gmail.com"
        val emailBody = """
        <h4>Hello,</h4>
        <p>Below is your verification code:</p>
        <h2>$verificationCode</h2>
        <p>Please enter this code in the app to verify your email.</p>
        <p><strong>Note:</strong> This code will expire in 5 minutes.</p>
        <br>
        <p>Best Regards,</p>
        <p><strong>Le Tien Dat</strong>, Developer of the System.</p>
    """.trimIndent()

        val jsonObject = JSONObject().apply {
            put("personalizations", JSONArray().put(JSONObject().apply {
                put("to", JSONArray().put(JSONObject().put("email", toEmail)))
                put("subject", "Verify Your Email")
            }))
            put("from", JSONObject().put("email", fromEmail))
            put("content", JSONArray().put(JSONObject().apply {
                put("type", "text/html")
                put("value", "Your verification code is $verificationCode.")
            }))
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.sendgrid.com/v3/mail/send")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Email", "Fail to send email")
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBody = response.body?.string() ?: "No response body"
                Log.d("Email", "Response: $responseBody")
            }
        })
    }


    fun showEmailVerificationDialog(context: Context, toEmail: String, onSuccess: (Boolean) -> Unit) {
        var correctCode = generateVerificationCode()
        sendVerificationEmail(toEmail, correctCode, context)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Verify Your Email")

        var start = Timestamp.now().seconds

        val view = LayoutInflater.from(context).inflate(R.layout.email_verification, null)
        val codeInput = view.findViewById<EditText>(R.id.codeInput)
        val resendEmailButton = view.findViewById<TextView>(R.id.resendEmailButton)

        builder.setView(view)

        resendEmailButton.setOnClickListener {
            correctCode = generateVerificationCode()
            start = Timestamp.now().seconds
            sendVerificationEmail(toEmail, correctCode, context)
        }

        builder.setPositiveButton("Verify") { _, _ ->
            val enteredCode = codeInput.text.toString()
            val now = Timestamp.now().seconds
            if (now - start > 600) {
                Toast.makeText(context, "Code expired. Try again.", Toast.LENGTH_SHORT).show()
                onSuccess(false)
                return@setPositiveButton
            }
            if (enteredCode == correctCode) {
                Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                onSuccess(true)
            } else {
                Toast.makeText(context, "Incorrect code. Try again.", Toast.LENGTH_SHORT).show()
                onSuccess(false)
            }
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            onSuccess(false)
        }
        builder.show()
    }
}

