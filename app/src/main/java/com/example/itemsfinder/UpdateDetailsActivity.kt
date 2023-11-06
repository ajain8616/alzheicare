package com.example.itemsfinder

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateDetailsActivity : AppCompatActivity() {
    private lateinit var editItemName: EditText
    private lateinit var editDescription: EditText
    private lateinit var editItemType: EditText
    private lateinit var saveChangesButton: ImageButton
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)

        editItemName = findViewById(R.id.editItemName)
        editDescription = findViewById(R.id.editDescription)
        editItemType = findViewById(R.id.editItemType)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        databaseReference = FirebaseDatabase.getInstance().reference

    }
}
