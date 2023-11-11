package com.example.itemsfinder

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContainerDetailsActivity : AppCompatActivity() {
    private lateinit var storeItemInContainer: ImageButton
    private lateinit var itemName: TextView
    private lateinit var description: TextView
    private lateinit var itemType: TextView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var editActionButton: FloatingActionButton
    private lateinit var deleteActionButton: FloatingActionButton
    private lateinit var objectSpinner: Spinner
    private lateinit var containerSpinner: Spinner
    private lateinit var objectSpinnerView: TextView
    private lateinit var containerSpinnerView: TextView

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
                editActionButton.visibility = View.VISIBLE
                deleteActionButton.visibility = View.VISIBLE
            } else {
                editActionButton.visibility = View.GONE
                deleteActionButton.visibility = View.GONE
            }
        }

        editActionButton.setOnClickListener {
            val intent = Intent(this, UpdateDetailsActivity::class.java)
            startActivity(intent)
        }
        deleteActionButton.setOnClickListener {

        }
    }

    private fun findIdsOfElements() {
        storeItemInContainer = findViewById(R.id.storeItemInContainer)
        itemName = findViewById(R.id.itemNameView)
        description = findViewById(R.id.descriptionView)
        itemType = findViewById(R.id.itemTypeView)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        editActionButton = findViewById(R.id.editActionButton)
        deleteActionButton = findViewById(R.id.deleteActionButton)
        objectSpinner = findViewById(R.id.objectSpinner)
        containerSpinner = findViewById(R.id.containerSpinner)
        objectSpinnerView = findViewById(R.id.objectSpinnerView)
        containerSpinnerView = findViewById(R.id.containerSpinnerView)
    }

    private fun displayData() {
        val itemNameExtra = intent.getStringExtra("itemName")
        val descriptionExtra = intent.getStringExtra("description")
        val itemTypeExtra = intent.getStringExtra("itemType")
        itemName.text = itemNameExtra
        itemName.setTypeface(null, Typeface.BOLD)
        description.text = descriptionExtra
        itemType.text = itemTypeExtra
    }

}
