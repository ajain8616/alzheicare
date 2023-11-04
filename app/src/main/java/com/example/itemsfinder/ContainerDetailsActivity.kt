package com.example.itemsfinder

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ContainerDetailsActivity : AppCompatActivity() {
    private lateinit var containerSpinner: Spinner
    private lateinit var selectedItemTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_details)

        containerSpinner = findViewById(R.id.containerSpinner)
        selectedItemTextView = findViewById(R.id.selectedItemTextView)

        // Replace this with your list of container options
        val containerOptions = arrayOf("Container A", "Container B", "Container C")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, containerOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        containerSpinner.adapter = adapter

        containerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedContainer = containerOptions[position]

                // Replace this with the logic to display the selected item in the container
                val selectedItem = getSelectedItemInContainer(selectedContainer)

                selectedItemTextView.text = "Item in $selectedContainer: $selectedItem"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle when nothing is selected (if needed)
            }
        }

        val intent = intent
        val itemName = intent.getStringExtra("itemName")
        val description = intent.getStringExtra("description")
        val itemType = intent.getStringExtra("itemType")

        val itemNameTextView = findViewById<TextView>(R.id.itemNameTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val itemTypeTextView = findViewById<TextView>(R.id.itemTypeTextView)

        itemNameTextView.text = "Item Name: $itemName"
        descriptionTextView.text = "Description: $description"
        itemTypeTextView.text = "Item Type: $itemType"
    }

    // Replace this with your logic to retrieve the selected item in the container
    private fun getSelectedItemInContainer(container: String): String {
        return "Selected Item in $container"
    }
}
