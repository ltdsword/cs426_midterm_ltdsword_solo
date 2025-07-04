package com.example.codecup

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfileManagement {
    fun isUsernameTaken(username: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                Log.d("Firestore", "Document Exists: ${document.exists()}, Data: ${document.data}")
                callback(document.exists()) // Returns true if the username exists
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking username", e)
                callback(false)
            }
    }

    fun getProfileHelper(username: String, callback: (Profile?) -> Unit) {
        Log.d("Firestore", "Fetching profile for username: $username")
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(Profile::class.java)
                    if (profile == null) {
                        Log.e("Firestore", "Failed to deserialize Profile")
                    }
                    callback(profile)
                } else {
                    Log.e("Firestore", "Document does not exist")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching profile", e)
                callback(null)
            }
    }

    suspend fun getProfile(username: String): Profile? {
        return suspendCoroutine { continuation ->
            getProfileHelper(username) { profile ->
                continuation.resume(profile)
            }
        }
    }

    fun isUsernameExist(username: String): Boolean {
        var exist = false
        isUsernameTaken(username) { callback ->
            if (!callback) {
                exist = true
            }
        }
        return exist
    }

    fun isLegalUsername(username: String): Boolean {
        val illegalChars = listOf('/', '\\', '#', '?', ' ')
        return username != "" && username.none { it in illegalChars }
    }

    fun getUsernameViaEmail(email: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val username = document.id
                    val data = document.data

                    var flag = false
                    for (key in data.keys) {
                        if (key == "email" && data[key] == email) {
                            flag = true
                            callback(username)
                            break
                        }
                    }

                    if (!flag) callback("")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    fun getProfileFromLocal(context: Context): Profile {
        val data = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val json = data.getString("profile", null)
        val profile = if (json != null) {
            Gson().fromJson(json, Profile::class.java)
        } else {
            Profile()
        }

        return profile
    }

    fun saveProfile(profile: Profile) {
        // save profile to Firebase
        val username = profile.name
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).set(profile)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile saved successfully for username: $username")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving profile", e)
            }
    }

    fun saveProfile(profile: Profile, context: Context) {
        val data = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = data.edit()
        val profileJson = Gson().toJson(profile)
        editor.putString("profile", profileJson)
        editor.putString("username", profile.name)
        editor.apply()
        saveProfile(profile)
    }

    fun deleteUsername(username: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).delete()
            .addOnSuccessListener {
                Log.d("Firestore", "User profile deleted successfully: $username")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting user profile", e)
            }
    }
}

