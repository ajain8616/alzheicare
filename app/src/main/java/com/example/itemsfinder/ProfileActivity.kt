package com.example.itemsfinder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var profile_picture: ImageView
    private lateinit var user_info: TextView
    private lateinit var user_status: TextView
    private lateinit var logout_button: ImageButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PERMISSION_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profile_picture = findViewById(R.id.profile_picture)
        user_info = findViewById(R.id.user_info)
        user_status = findViewById(R.id.user_status)
        logout_button = findViewById(R.id.logout_button)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.reference.child("users")

        // Get the current user's email ID
        val currentUser = mAuth.currentUser
        val email = currentUser?.email

        // Set the email ID in the user_info TextView
        user_info.text = email

        // Set the user's status in the user_status TextView
        val status = getUserStatus()
        user_status.text = status

        // Push the user's status to Firebase
        pushUserStatus(status)

        profile_picture.setOnClickListener {
            // Check if permission to access external storage is granted
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission if not granted
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission already granted, open the gallery
                openGallery()
            }
        }

        logout_button.setOnClickListener {
            logout()
        }
    }

    private fun getUserStatus(): String {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return when {
            networkInfo == null -> "Offline"
            networkInfo.isConnected -> "Online"
            else -> "Working"
        }
    }

    private fun pushUserStatus(status: String) {
        val currentUser = mAuth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            userRef.child("status").setValue(status)
        }
    }

    private fun logout() {
        mAuth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the gallery
                openGallery()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            // Update the profile picture with the selected image
            profile_picture.setImageURI(imageUri)
            // Save the image to Firebase or perform any other necessary operations
            saveProfilePicture(imageUri)
        }
    }

    private fun saveProfilePicture(imageUri: Uri?) {
        // Get the current user's ID
        val currentUser = mAuth.currentUser
        val userId = currentUser?.uid

        if (userId != null && imageUri != null) {
            val userRef = database.getReference("users").child(userId)
            // Save the image URL to Firebase
            userRef.child("profilePicture").setValue(imageUri.toString())
        }
    }
}

