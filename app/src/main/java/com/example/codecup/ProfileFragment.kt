package com.example.codecup

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.card.MaterialCardView
import java.io.File


class ProfileFragment : Fragment() {

    private lateinit var profile: Profile
    private val profileManagement = ProfileManagement()
    private val uploadImage = UploadImage()
    private lateinit var imageView: ImageView

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImage.uploadImageToFirebase(requireContext(), it, profile, imageView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<MaterialCardView>(R.id.bottom_navi_bar)?.visibility = View.GONE

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        profile = profileManagement.getProfileFromLocal(requireContext())

        val fullNameText = view.findViewById<EditText>(R.id.fullNameText)
        val phoneNumber = view.findViewById<EditText>(R.id.phoneNumber)
        val emailText = view.findViewById<EditText>(R.id.emailText)
        val addressText = view.findViewById<EditText>(R.id.addressText)

        fullNameText.setText(profile.name)
        phoneNumber.setText(profile.phoneNumber)
        emailText.setText(profile.email)
        addressText.setText(profile.address)

        imageView = view.findViewById(R.id.avatar)
        val changeAvatar = view.findViewById<Button>(R.id.changeAvatar)
        changeAvatar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        uploadImage.loadAvatarFromUrl(profile.avatarUrl, requireContext(), imageView)

        val logoutButton = view.findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Clear SharedPreferences
                    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        remove("username")
                        remove("profile")
                        putBoolean("isLoggedIn", false)
                        apply()
                    }

                    // Finish current activity and start LoginActivity
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        val editName = view.findViewById<ImageView>(R.id.editName)
        val editPhone = view.findViewById<ImageView>(R.id.editPhone)
        val editEmail = view.findViewById<ImageView>(R.id.editEmail)
        val editAddress = view.findViewById<ImageView>(R.id.editAddress)

        setupEditableField(
            editIcon = editName,
            editText = fullNameText,
            onSave = { newText ->
                if (profile.name != newText) {
                    uploadImage.renameUserAvatarFolder(profile.name, newText) { success ->
                        if (success) {
                            profile.name = newText
                            profileManagement.saveProfile(profile, requireContext())
                        }
                    }
                }
            }
        )

        setupEditableField(
            editIcon = editPhone,
            editText = phoneNumber,
            onSave = { newText ->
                profile.phoneNumber = newText
                profileManagement.saveProfile(profile, requireContext())
            }
        )

        setupEditableField(
            editIcon = editEmail,
            editText = emailText,
            onSave = { newText ->
                val emailVerify = EmailVerify()
                if (emailVerify.isValidEmail(newText)) {
                    emailVerify.showEmailVerificationDialog(requireContext(), newText) { success ->
                        if (success) {
                            profile.email = newText
                            profileManagement.saveProfile(profile, requireContext())
                        }
                        else {
                            Toast.makeText(requireContext(), "Email verification failed.", Toast.LENGTH_SHORT).show()
                            emailText.setText(profile.email)
                        }
                    }
                }
                else {
                    Toast.makeText(requireContext(), "Invalid email format.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        setupEditableField(
            editIcon = editAddress,
            editText = addressText,
            onSave = { newText ->
                profile.address = newText
                profileManagement.saveProfile(profile, requireContext())
            }
        )
    }

    private fun scanFile(context: Context, file: File) {
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
    }

    private fun setupEditableField(
        editIcon: ImageView,
        editText: EditText,
        onSave: (String) -> Unit
    ) {
        var editable = false

        editIcon.setOnClickListener {
            editable = !editable
            editText.isEnabled = editable
            editText.isFocusable = editable
            editText.isFocusableInTouchMode = editable
            if (editable) {
                editIcon.setImageResource(R.drawable.check)
                editText.requestFocus()
                //showKeyboard(editText)
            } else {
                editIcon.setImageResource(R.drawable.edit)
                //hideKeyboard(editText)
                onSave(editText.text.toString().trim())
            }
        }

        // Start as non-editable
        editText.isEnabled = false
        editText.isFocusable = false
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onStop() {
        super.onStop()
        // save the profile data
        profileManagement.saveProfile(profile, requireContext())
    }
}
