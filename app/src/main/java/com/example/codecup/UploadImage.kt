package com.example.codecup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.codecup.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class UploadImage {
    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    private fun resizeImage(context: Context, uri: Uri, width: Int, height: Int): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true) // Resize to width x height

        val baos = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val imageData = baos.toByteArray()

        return imageData
    }

    fun uploadImageToFirebase(context: Context, uri: Uri, profile: Profile, avatarImageView: ImageView) {
        var fileName = "avatars/${profile.name}/profile_pic_normal.jpg"
        var avatarRef = storageRef.child(fileName)

        val imageData100 = resizeImage(context, uri, 400, 400)
        val imageData40 = resizeImage(context, uri, 160, 160)

        avatarRef.putBytes(imageData100)
            .addOnSuccessListener {
                avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val avatarUrl = downloadUri.toString()

                    val profileManagement = ProfileManagement()
                    profile.avatarUrl = avatarUrl
                    profileManagement.saveProfile(profile, context)

                    Log.d("Upload", "Avatar loaded successfully, url: $avatarUrl")
                    loadAvatarFromUrl(avatarUrl, context, avatarImageView)
                }
            }
            .addOnFailureListener {
                Log.d("Upload", "Upload failed")
                Toast.makeText(context, "Upload failed 62: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        fileName = "avatars/${profile.name}/profile_pic_small.jpg"
        avatarRef = storageRef.child(fileName)
        avatarRef.putBytes(imageData40)
            .addOnSuccessListener {
                avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val avatarUrlSmall = downloadUri.toString()

                    val profileManagement = ProfileManagement()
                    profile.avatarUrlSmall = avatarUrlSmall
                    profileManagement.saveProfile(profile, context)

                    Log.d("Upload", "Avatar loaded successfully, url: $avatarUrlSmall")
                    loadAvatarFromUrl(avatarUrlSmall, context, avatarImageView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Upload failed 81: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


     fun loadAvatarFromUrl(url: String, context: Context, avatarImageView: ImageView) {
        Glide.with(context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .placeholder(R.mipmap.default_avatar)
            .error(R.mipmap.default_avatar)
            .into(avatarImageView)
    }

    fun loadDefaultAvatar(context: Context, avatarImageView: ImageView) {
        Glide.with(context)
            .load(R.mipmap.default_avatar)
            .circleCrop()
            .into(avatarImageView)
    }

    fun loadUserAvatar(context: Context, profile: Profile, avatarImageView: ImageView, type: String = "normal") {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileName = "avatars/${profile.name}/profile_pic_$type.jpg"
        val avatarRef = FirebaseStorage.getInstance().getReference(fileName)

        avatarRef.downloadUrl
            .addOnSuccessListener { uri ->
                loadAvatarFromUrl(uri.toString(), context, avatarImageView)
                Log.d("Upload", "User avatar loaded successfully, url: $uri")
            }
            .addOnFailureListener {
                loadDefaultAvatar(context, avatarImageView)
                Log.e("Upload", "Failed to load user avatar", it)
            }
    }

    fun renameUserAvatarFolder(oldUsername: String, newUsername: String, onComplete: (Boolean) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val oldFolder = "avatars/$oldUsername/"
        val newFolder = "avatars/$newUsername/"

        val oldFolderRef = storage.reference.child(oldFolder)

        // List all files under old folder
        oldFolderRef.listAll()
            .addOnSuccessListener { listResult ->
                val items = listResult.items
                if (items.isEmpty()) {
                    Log.w("RenameAvatar", "No files found in old folder.")
                    onComplete(true)
                    return@addOnSuccessListener
                }

                var completed = 0
                var success = true

                fun checkComplete() {
                    completed++
                    if (completed == items.size) {
                        onComplete(success)
                    }
                }

                for (item in items) {
                    val fileName = item.name
                    val newFileRef = storage.reference.child("$newFolder$fileName")

                    item.downloadUrl.addOnSuccessListener { uri ->
                        // Download original file and upload to new location
                        val client = OkHttpClient()
                        val request = Request.Builder().url(uri.toString()).build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.e("RenameAvatar", "Failed to download ${item.name}: ${e.message}")
                                success = false
                                checkComplete()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (!response.isSuccessful) {
                                    Log.e("RenameAvatar", "Failed to fetch ${item.name}: ${response.message}")
                                    success = false
                                    checkComplete()
                                    return
                                }

                                val bytes = response.body?.bytes()
                                if (bytes == null) {
                                    Log.e("RenameAvatar", "Empty file: ${item.name}")
                                    success = false
                                    checkComplete()
                                    return
                                }

                                newFileRef.putBytes(bytes)
                                    .addOnSuccessListener {
                                        // Delete old file after copy
                                        item.delete()
                                            .addOnSuccessListener {
                                                Log.d("RenameAvatar", "Moved ${item.name} to $newFolder")
                                                checkComplete()
                                            }
                                            .addOnFailureListener {
                                                Log.e("RenameAvatar", "Failed to delete old file ${item.name}")
                                                success = false
                                                checkComplete()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Log.e("RenameAvatar", "Failed to upload ${item.name} to new folder")
                                        success = false
                                        checkComplete()
                                    }
                            }
                        })
                    }.addOnFailureListener {
                        Log.e("RenameAvatar", "Could not get download URL for ${item.name}")
                        success = false
                        checkComplete()
                    }
                }
            }
            .addOnFailureListener {
                Log.e("RenameAvatar", "Failed to list old avatar folder: ${it.message}")
                onComplete(false)
            }
    }
}