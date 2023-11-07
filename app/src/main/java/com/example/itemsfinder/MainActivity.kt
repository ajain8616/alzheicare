package com.example.itemsfinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    private var isAddItemLayoutVisible = false
    private var isSearchItemVisible = false
    private lateinit var addItem: ImageButton
    private lateinit var searchItem: ImageButton
    private lateinit var sendItem: ImageButton
    private lateinit var itemName: EditText
    private lateinit var description: EditText
    private lateinit var itemSearch:EditText
    private lateinit var itemTypeRadioGroup: RadioGroup
    private lateinit var radioObject: RadioButton
    private lateinit var radioContainer: RadioButton
    private lateinit var itemListView: RecyclerView
    private lateinit var addItemLayout: RelativeLayout
    private lateinit var searchItemLayout: RelativeLayout
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var itemList: MutableList<Item>
    private lateinit var filteredItemList: MutableList<Item>
    private lateinit var database: DatabaseReference
    private lateinit var fabActionButton:FloatingActionButton
    private lateinit var profileActionButton:FloatingActionButton
    private lateinit var cameraActionButton:FloatingActionButton



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
        itemSearch=findViewById(R.id.itemSearch)
        itemListView = findViewById(R.id.itemsListView)
        addItemLayout = findViewById(R.id.addItemLayout)
        radioContainer = findViewById(R.id.radioContainer)
        itemTypeRadioGroup = findViewById(R.id.itemTypeRadioGroup)
        radioObject = findViewById(R.id.radioObject)
        searchItemLayout = findViewById(R.id.searchItemLayout)
        fabActionButton=findViewById(R.id.fabActionButton)
        profileActionButton=findViewById(R.id.profileActionButton)
        cameraActionButton=findViewById(R.id.cameraActionButton)
    }

    private fun setEventHandlers() {
        findIdsOfElements()
        addItem.setOnClickListener {
            if (addItemLayout.visibility == View.GONE) {
                addItemLayout.visibility = View.VISIBLE // Show the form
                isAddItemLayoutVisible = true
            } else {
                addItemLayout.visibility = View.GONE // Hide the form
                isAddItemLayoutVisible = false
            }
            searchItemLayout.visibility = View.GONE
            isSearchItemVisible = false
            itemSearch.text = null

        }
        searchItem.setOnClickListener {
            if (searchItemLayout.visibility == View.GONE) {
                searchItemLayout.visibility = View.VISIBLE // Show the search layout
                isSearchItemVisible = true
            } else {
                searchItemLayout.visibility = View.GONE // Hide the search layout
                isSearchItemVisible = false
            }

            addItemLayout.visibility = View.GONE
            isAddItemLayoutVisible = false
            itemSearch.text = null
            filterItems("")
        }

        itemSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterItems(s.toString())
            }
        })


        fabActionButton.setOnClickListener {
            if (profileActionButton.visibility == View.GONE || cameraActionButton.visibility == View.GONE) {
                // Expand the buttons
                profileActionButton.visibility = View.VISIBLE
                cameraActionButton.visibility = View.VISIBLE
            } else {
                // Collapse the buttons
                profileActionButton.visibility = View.GONE
                cameraActionButton.visibility = View.GONE
            }
        }



        profileActionButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
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
            saveItemListToSharedPreferences()
        }
    }

    private fun setupRecyclerView() {
        itemList = loadItemListFromSharedPreferences()
        filteredItemList = mutableListOf()
        filteredItemList.addAll(itemList)
        itemListAdapter = ItemListAdapter(filteredItemList)
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

    private fun saveItemListToSharedPreferences() {
        val sharedPreferences = getSharedPreferences("ItemListPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val jsonArray = JSONArray()
        for (item in itemList) {
            jsonArray.put(item.toJson())
        }
        editor.putString("itemList", jsonArray.toString())
        editor.apply()
    }

    private fun loadItemListFromSharedPreferences(): MutableList<Item> {
        val sharedPreferences = getSharedPreferences("ItemListPrefs", MODE_PRIVATE)
        val itemListJson = sharedPreferences.getString("itemList", null)
        val itemList = mutableListOf<Item>()

        if (itemListJson != null) {
            val jsonArray = JSONArray(itemListJson)

            // Convert the JSONArray to a list of JSONObjects
            val jsonObjectList = (0 until jsonArray.length())
                .map { jsonArray.getJSONObject(it) }

            // Sort the list of JSONObjects by the "itemName" field
            val sortedJSONObjectList = jsonObjectList.sortedBy { it.optString("itemName") }

            for (itemJson in sortedJSONObjectList) {
                val itemName = itemJson.optString("itemName")
                val description = itemJson.optString("description")
                val itemType = itemJson.optString("itemType")
                val item = Item(itemName, description, itemType)
                itemList.add(item)
            }
        }

        return itemList
    }

    private fun filterItems(searchText: String) {
        filteredItemList.clear()
        for (item in itemList) {
            if (item.itemName.contains(searchText, ignoreCase = true)) {
                filteredItemList.add(item)
            }
        }

        itemListAdapter.notifyDataSetChanged()
    }

}
