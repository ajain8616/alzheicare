package com.example.itemsfinder

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ProfileActivity : AppCompatActivity() {
    private lateinit var user_mail:TextView
    private lateinit var elementsCount:TextView
    private lateinit var objectsCount:TextView
    private lateinit var containersCount:TextView
    private lateinit var logout_button: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        eventHandler()
    }

    private fun elementsId() {
        user_mail = findViewById(R.id.user_mail)
        elementsCount = findViewById(R.id.elementsCount)
        objectsCount = findViewById(R.id.objectsCount)
        containersCount = findViewById(R.id.containersCount)
        logout_button = findViewById(R.id.logout_button)
    }

    private fun eventHandler() {
        elementsId()

        // Get the current user
        val user = FirebaseAuth.getInstance().currentUser
        user_mail.text = user?.email

        // Update element counts
        if (user != null) {
            updateElementCounts()
        }

        logout_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun updateElementCounts() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val collectionName = "Item_Container_Data"

        if (userId != null) {
            database.child(collectionName).child(userId).get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val items = dataSnapshot.children.mapNotNull { it.getValue(Item::class.java) }
                        val totalElements = items.size
                        elementsCount.text = "TOTAL ELEMENTS = $totalElements"
                        var totalObjects = 0
                        var totalContainers = 0
                        for (item in items) {
                            if (item.itemType == "OBJECT") {
                                totalObjects++
                            } else if (item.itemType == "CONTAINER") {
                                totalContainers++
                            }
                        }
                        objectsCount.text = "TOTAL OBJECTS = $totalObjects"
                        containersCount.text = "TOTAL CONTAINERS = $totalContainers"
                    } else {
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
        } else {
        }
    }


}

