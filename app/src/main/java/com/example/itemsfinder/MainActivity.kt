package com.example.itemsfinder

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var addItem: ImageButton
    private lateinit var searchItem: ImageButton
    private lateinit var sendItem: ImageButton
    private lateinit var itemName: EditText
    private lateinit var description: EditText
    private lateinit var itemType: Spinner
    private lateinit var itemSearch:EditText
    private lateinit var itemListView: RecyclerView
    private lateinit var addItemForm: RelativeLayout
    private lateinit var searchItemLayout:RelativeLayout
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var itemList: MutableList<Item>
    private lateinit var database: DatabaseReference


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
                val searchTerm = itemSearch.text.toString()
                performSearch(searchTerm)
            }
        }

        sendItem.setOnClickListener {
            val name = itemName.text.toString()
            val desc = description.text.toString()
            val type = itemType.selectedItem.toString()
            val item = Item(name, desc, type)
            itemList.add(item)
            itemListAdapter.notifyDataSetChanged()

            // Store the data in Firebase
            setDataOnFirebase(item)
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
   private fun performSearch(searchTerm: String) {
        val searchQuery = database.child("Item_Container_Details")
            .orderByChild("itemName")
            .equalTo(searchTerm)

        searchQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    item?.let {
                        itemList.add(it)
                    }
                }

                itemListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@MainActivity, "Failed to perform search", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
