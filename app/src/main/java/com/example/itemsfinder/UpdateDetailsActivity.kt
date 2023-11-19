package com.example.itemsfinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UpdateDetailsActivity : AppCompatActivity() {
    private lateinit var editItemName: EditText
    private lateinit var editDescription: EditText
    private lateinit var editItemType: EditText
    private lateinit var saveChangesButton: ImageButton
    private lateinit var updateMessage:LottieAnimationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)

        editItemName = findViewById(R.id.editItemName)
        editDescription = findViewById(R.id.editDescription)
        editItemType = findViewById(R.id.editItemType)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        updateMessage=findViewById(R.id.updateMessage)

        saveChangesButton.setOnClickListener {

        }

    }
}
