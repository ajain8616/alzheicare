package com.example.alzheicare

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {
   
    private lateinit var userMail: TextView
    private lateinit var elementsCount: TextView
    private lateinit var objectsCount: TextView
    private lateinit var containersCount: TextView
    private lateinit var profileImage: ImageView
    private lateinit var logoutButton: Button
    private lateinit var back_button: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var currentUser: FirebaseUser

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initializeViews()
        setClickListeners()
        loadUserData()
        loadElementCounts()
    }

    private fun initializeViews() {
        userMail = findViewById(R.id.user_mail)
        elementsCount = findViewById(R.id.elementsCount)
        objectsCount = findViewById(R.id.objectsCount)
        containersCount = findViewById(R.id.containersCount)
        profileImage = findViewById(R.id.user_image)
        logoutButton = findViewById(R.id.logout_button_id)
        back_button = findViewById(R.id.back_button_image)
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        currentUser = FirebaseAuth.getInstance().currentUser!!
    }

    private fun setClickListeners() {
        profileImage.setOnClickListener {
            openFileChooser()
        }

        back_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            val imageUri = data.data
            if (imageUri != null) {
                uploadImage(imageUri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val userId = currentUser.uid

        val fileRef = storage.child("Profile_Images").child("$userId.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                Toast.makeText(
                    this@ProfileActivity,
                    "Image uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
                // Update profile image in ImageView
                profileImage.setImageURI(imageUri)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@ProfileActivity,
                    "Upload failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadElementCounts() {
        val userId = currentUser.uid

        database.child("Item_Container_Data").child(userId).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val items = dataSnapshot.children.mapNotNull { it.getValue(Item::class.java) }
                    val totalElements = items.size
                    elementsCount.text = "Total Elements: $totalElements"
                    var totalObjects = 0
                    var totalContainers = 0
                    for (item in items) {
                        if (item.itemType == "OBJECT") {
                            totalObjects++
                        } else if (item.itemType == "CONTAINER") {
                            totalContainers++
                        }
                    }
                    objectsCount.text = "Objects: $totalObjects"
                    containersCount.text = "Containers: $totalContainers"
                } else {
                    // Handle case where no data exists
                }
            }
            .addOnFailureListener { e ->
                // Handle failure, show error message
                Toast.makeText(
                    this@ProfileActivity,
                    "Error getting data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadUserData() {
        userMail.text = currentUser.email
    }
}
