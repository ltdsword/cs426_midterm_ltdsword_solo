package com.example.codecup

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.AEADBadTagException


class MyApplication : Application() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = try {
            createEncryptedPrefs()
        } catch (e: AEADBadTagException) {
            // In case of corruption, delete and recreate
            deleteSharedPreferences("secure_prefs")
            createEncryptedPrefs()
        }

        // Save sensitive keys if not already saved
        val sendgridKey = BuildConfig.SENDGRID_API_KEY
        if (!sharedPreferences.contains("API_KEY")) {
            sharedPreferences.edit().putString("API_KEY", sendgridKey).apply()
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        // MasterKey is still required to work with EncryptedSharedPreferences
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            this,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
