package com.example.itemsfinder

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), ItemListAdapter.OnItemDeleteListener {
    private lateinit var addItem: ImageButton
    private lateinit var searchItem: ImageButton
    private lateinit var sendItem: ImageButton
    private lateinit var itemName: EditText
    private lateinit var description: EditText
    private lateinit var itemType: Spinner
    private lateinit var itemSearch: EditText
    private lateinit var itemListView: RecyclerView
    private lateinit var addItemForm: RelativeLayout
    private lateinit var searchItemLayout: RelativeLayout
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var itemList: MutableList<Item>
    private lateinit var database: DatabaseReference
    private lateinit var typeImg: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = FirebaseDatabase.getInstance().reference
        setEventHandlers()
        setupSpinner()
        setupRecyclerView()
    }

    private fun findIdsOfElements() {
        addItem = findViewById(R.id.addItemButton)
        searchItem = findViewById(R.id.searchButton)
        sendItem = findViewById(R.id.sendButton)
        itemName = findViewById(R.id.itemName)
        description = findViewById(R.id.description)
        itemType = findViewById(R.id.itemTypeSpinner)
        itemListView = findViewById(R.id.itemListView)
        addItemForm = findViewById(R.id.addItemForm)
        searchItemLayout=findViewById(R.id.searchItemLayout)
        itemSearch=findViewById(R.id.itemSearch)
        typeImg=findViewById(R.id.typeImg)

    }

    private fun setEventHandlers() {
        findIdsOfElements()
        addItem.setOnClickListener {
            if (addItemForm.visibility == View.GONE) {
                addItemForm.visibility = View.VISIBLE // Show the form
            } else {
                addItemForm.visibility = View.GONE // Hide the form
            }
        }

        searchItem.setOnClickListener {
            if (searchItemLayout.visibility == View.GONE) {
                searchItemLayout.visibility = View.VISIBLE // Show the form
            } else {
                searchItemLayout.visibility = View.GONE // Hide the form
            }
        }

        sendItem.setOnClickListener {
            val name = itemName.text.toString()
            val desc = description.text.toString()
            val type = itemType.selectedItem.toString()
            val item = Item(name, desc, type)
            itemList.add(item)
            itemListAdapter.notifyDataSetChanged()
            setDataOnFirebase(item)
        }

        itemType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "OBJECT") {
                    typeImg.setImageResource(R.drawable.icon_object)
                } else if (selectedItem == "CONTAINER") {
                    typeImg.setImageResource(R.drawable.icon_container)
                } else {
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

    }

    private fun setupSpinner() {
        val itemTypeOptions = arrayOf("Choose your Type:-- ", "OBJECT", "CONTAINER")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemTypeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemType.adapter = adapter
        itemType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Perform additional logic based on the selected option
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    private fun setupRecyclerView() {
        itemList = mutableListOf()
        itemListAdapter = ItemListAdapter(itemList)
        itemListView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = itemListAdapter
        }

        itemListAdapter.setOnItemDeleteListener(this) // Set the listener here
    }

    private fun setDataOnFirebase(item: Item) {
        val newItemRef = database.child("Item_Container_Details").push()
        newItemRef.setValue(item)
            .addOnSuccessListener {
                Toast.makeText(this, "Data added to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add data to Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemDelete(item: Item) {
        val itemIndex = itemList.indexOf(item)
        if (itemIndex != -1) {
            itemList.removeAt(itemIndex)
            itemListAdapter.notifyDataSetChanged()
        }
    }
}
