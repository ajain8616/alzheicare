package com.example.itemsfinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Toast
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
    private lateinit var itemTypeRadioGroup:RadioGroup
    private lateinit var radioObject:RadioButton
    private lateinit var radioContainer:RadioButton
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
        setupRecyclerView()
    }

    private fun findIdsOfElements() {
        addItem = findViewById(R.id.addItemButton)
        searchItem = findViewById(R.id.searchButton)
        sendItem = findViewById(R.id.sendButton)
        itemName = findViewById(R.id.itemName)
        description = findViewById(R.id.description)
        itemListView = findViewById(R.id.itemsListView)
        addItemForm = findViewById(R.id.addItemForm)
        radioContainer=findViewById(R.id.radioContainer)
        itemTypeRadioGroup=findViewById(R.id.itemTypeRadioGroup)
        radioObject=findViewById(R.id.radioObject)
        searchItemLayout=findViewById(R.id.searchItemLayout)

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
                searchItemLayout.visibility = View.VISIBLE // Show the search layout
            } else {
                searchItemLayout.visibility = View.GONE // Hide the search layout
            }
        }

        sendItem.setOnClickListener {
            val itemName = itemName.text.toString()
            val description = description.text.toString()
            val itemType = when (itemTypeRadioGroup.checkedRadioButtonId) {
                R.id.radioObject -> "OBJECT"
                R.id.radioContainer -> "CONTAINER"
                else -> ""
            }
            val item = Item(itemName, description, itemType)
            itemList.add(item)
            itemListAdapter.notifyDataSetChanged()
            setDataOnFirebase(item)
        }
    }



    private fun setupRecyclerView() {
        itemList = mutableListOf()
        itemListAdapter = ItemListAdapter(itemList)
        itemListView.apply {
            adapter = itemListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
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

}
