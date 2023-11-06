package com.example.itemsfinder

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ContainerDetailsActivity : AppCompatActivity() {
    private lateinit var containerSpinner: Spinner
    private lateinit var storeItemInContainer:ImageButton
    private lateinit var selectedItemTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_details)

        containerSpinner = findViewById(R.id.containerSpinner)
        storeItemInContainer=findViewById(R.id.storeItemInContainer)
        selectedItemTextView = findViewById(R.id.selectedItemTextView)

    }
}
