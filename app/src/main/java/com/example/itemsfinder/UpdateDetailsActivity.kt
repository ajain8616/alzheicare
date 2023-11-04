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

        saveChangesButton.setOnClickListener {
            val newName = editItemName.text.toString()
            val newDescription = editDescription.text.toString()
            val newItemType = editItemType.text.toString()

            updateItemContainerDetails(newName, newDescription, newItemType)

            createUpdatedItemContainerDocument(newName, newDescription, newItemType)

            // Update the RecyclerView (notify adapter of the change)
            // You should have a reference to the RecyclerView adapter here
            // adapter.notifyDataSetChanged() // Replace 'adapter' with your actual adapter reference
            finish()
        }
    }

    private fun updateItemContainerDetails(newName: String, newDescription: String, newItemType: String) {
        // Replace with the logic to update the 'Item_Container_Details' collection
        // You may need to identify the specific document you want to update
        // and use 'update' or 'set' methods to modify its data
        // Example: databaseReference.child("Item_Container_Details").child(itemId).update(...)
        // Make sure to pass the itemId to identify the specific document.
    }

    private fun createUpdatedItemContainerDocument(newName: String, newDescription: String, newItemType: String) {
        val newItemRef = databaseReference.child("Updated_Item_Container_Details").push()
        val newItemData = mapOf(
            "name" to newName,
            "description" to newDescription,
            "type" to newItemType
        )
        newItemRef.setValue(newItemData)

        Toast.makeText(this, "Data updated and saved in Firebase", Toast.LENGTH_SHORT).show()
    }
}
