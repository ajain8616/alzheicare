package com.example.itemsfinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ContainerDetailsActivity : AppCompatActivity() {
    private lateinit var containerSpinner: Spinner
    private lateinit var storeItemInContainer: ImageButton
    private lateinit var itemName: TextView
    private lateinit var description: TextView
    private lateinit var itemType: TextView
    private lateinit var selectedItemTextView: TextView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var editActionButton: FloatingActionButton
    private lateinit var deleteActionButton: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_details)
        setEventHandlers()
        displayData()
    }

    private fun setEventHandlers() {
        findIdsOfElements()

        floatingActionButton.setOnClickListener {
            if (editActionButton.visibility == View.GONE || deleteActionButton.visibility == View.GONE) {
                // Expand the buttons
                editActionButton.visibility = View.VISIBLE
                deleteActionButton.visibility = View.VISIBLE
            } else {
                // Collapse the buttons
                editActionButton.visibility = View.GONE
                deleteActionButton.visibility = View.GONE
            }
        }

        editActionButton.setOnClickListener {
            val intent = Intent(this, UpdateDetailsActivity::class.java)
            startActivity(intent)
        }
        // Assuming you have a reference to the itemList and an adapter for displaying it
        deleteActionButton.setOnClickListener {

        }

    }



        private fun findIdsOfElements()
    {
        containerSpinner = findViewById(R.id.containerSpinner)
        storeItemInContainer = findViewById(R.id.storeItemInContainer)
        itemName = findViewById(R.id.itemNameView)
        description = findViewById(R.id.descriptionView)
        itemType = findViewById(R.id.itemTypeView)
        selectedItemTextView = findViewById(R.id.selectedItemTextView)
        floatingActionButton=findViewById(R.id.floatingActionButton)
        editActionButton=findViewById(R.id.editActionButton)
        deleteActionButton=findViewById(R.id.deleteActionButton)
    }
    private fun displayData() {
        val itemNameExtra = intent.getStringExtra("itemName")
        val descriptionExtra = intent.getStringExtra("description")
        val itemTypeExtra = intent.getStringExtra("itemType")
        itemName.text = "Item_Name: $itemNameExtra"
        description.text ="Description: $descriptionExtra"
        itemType.text = "Item_Type: $itemTypeExtra"
    }


}
