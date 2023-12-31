package com.example.alzheicare

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {

    private var isAddItemLayoutVisible = false
    private var isSearchItemVisible = false
    private var isItemListVisible = false
    private lateinit var addItem: ImageButton
    private lateinit var searchItem: ImageButton
    private lateinit var sendItem: ImageButton
    private lateinit var itemName: EditText
    private lateinit var description: EditText
    private lateinit var itemSearch: EditText
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
    private lateinit var fabActionButton: FloatingActionButton
    private lateinit var profileActionButton: FloatingActionButton
    private lateinit var listViewActionButton: FloatingActionButton
    private lateinit var clearButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        setEventHandlers()
        setupRecyclerView()
        checkInternetConnection()
        itemListView.visibility=View.VISIBLE
    }


    private fun findIdsOfElements() {
        addItem = findViewById(R.id.addItemButton)
        searchItem = findViewById(R.id.searchButton)
        sendItem = findViewById(R.id.sendButton)
        itemName = findViewById(R.id.itemName)
        description = findViewById(R.id.description)
        itemSearch = findViewById(R.id.itemSearch)
        itemListView = findViewById(R.id.itemsListView)
        addItemLayout = findViewById(R.id.addItemLayout)
        radioContainer = findViewById(R.id.radioContainer)
        itemTypeRadioGroup = findViewById(R.id.itemTypeRadioGroup)
        radioObject = findViewById(R.id.radioObject)
        searchItemLayout = findViewById(R.id.searchItemLayout)
        fabActionButton = findViewById(R.id.fabActionButton)
        profileActionButton = findViewById(R.id.profileActionButton)
        listViewActionButton = findViewById(R.id.listViewActionButton)
        clearButton = findViewById(R.id.clearButton)
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
            itemListView.visibility = View.GONE
        }

        searchItem.setOnClickListener {
            if (searchItemLayout.visibility == View.GONE) {
                searchItemLayout.visibility = View.VISIBLE // Show the search layout
                isSearchItemVisible = true
            } else {
                searchItemLayout.visibility = View.GONE
                isSearchItemVisible = false
            }
            addItemLayout.visibility = View.GONE
            itemListView.visibility = View.VISIBLE

        }

        itemSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    val filteredItems = itemList.filter {
                        it.itemName.contains(searchText, ignoreCase = true) ||
                                it.description.contains(searchText, ignoreCase = true) ||
                                it.itemType.contains(searchText, ignoreCase = true)
                    }

                    filteredItemList.clear()
                    filteredItemList.addAll(filteredItems)
                    itemListAdapter.notifyDataSetChanged()
                } else {
                    filteredItemList.clear()
                    filteredItemList.addAll(itemList)
                    itemListAdapter.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fabActionButton.setOnClickListener {
            if (profileActionButton.visibility == View.GONE || listViewActionButton.visibility == View.GONE) {
                // Expand the buttons
                profileActionButton.visibility = View.VISIBLE
                listViewActionButton.visibility = View.VISIBLE
            } else {
                // Collapse the buttons
                profileActionButton.visibility = View.GONE
                listViewActionButton.visibility = View.GONE

            }
        }

        profileActionButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        listViewActionButton.setOnClickListener {
            if (itemListView.visibility == View.VISIBLE) {
                itemListView.visibility = View.GONE
                isItemListVisible = false
                Toast.makeText(
                    this@MainActivity,
                    "Item List View is now hidden",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                itemListView.visibility = View.VISIBLE
                isItemListVisible = true
                Toast.makeText(
                    this@MainActivity,
                    "Item List View is now visible",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        sendItem.setOnClickListener {
            val itemNameText = itemName.text.toString()
            val descriptionText = description.text.toString()
            val itemType = when (itemTypeRadioGroup.checkedRadioButtonId) {
                R.id.radioObject -> "OBJECT"
                R.id.radioContainer -> "CONTAINER"
                else -> ""
            }
            val isDuplicateItem =
                itemList.any { it.itemName.equals(itemNameText, ignoreCase = true) }

            if (itemNameText.isEmpty() || descriptionText.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "Item name and description cannot be empty",
                    Toast.LENGTH_LONG
                ).show()
            } else if (isDuplicateItem) {
                Toast.makeText(
                    this@MainActivity,
                    "Item already exists in the list",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val item = Item(itemNameText, descriptionText, itemType,)
                itemList.add(item)
                itemListAdapter.notifyDataSetChanged()
                Toast.makeText(this@MainActivity, "Item added successfully", Toast.LENGTH_LONG)
                    .show()

                itemName.text.clear()
                description.text.clear()
            }
            setDataToFirebase()
            itemListView.visibility = View.VISIBLE
            addItemLayout.visibility = View.GONE
        }


        clearButton.setOnClickListener {
            itemSearch.text.clear()
        }

        if (currentUser != null) {
            getDataFromFirebase()
        }
    }

    private fun setupRecyclerView() {
        itemList = mutableListOf()
        filteredItemList = mutableListOf()
        itemListAdapter = ItemListAdapter(filteredItemList)
        itemListView.apply {
            adapter = itemListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            visibility=View.VISIBLE
        }
        getDataFromFirebase()
    }

    private fun setDataToFirebase() {
        val userId = currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val collectionName = "Item_Container_Data"
        if (userId != null) {
            val itemsMap = itemList.map { it.itemName to it }.toMap()
            database.child(collectionName).child(userId).setValue(itemsMap)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@MainActivity,
                        "Data uploaded successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@MainActivity,
                        "Error uploading data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(
                this@MainActivity,
                "User not logged in. Data cannot be uploaded.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun getDataFromFirebase() {
        val userId = currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val collectionName = "Item_Container_Data"

        if (userId != null) {
            database.child(collectionName).child(userId).get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val items =
                            dataSnapshot.children.mapNotNull { it.getValue(Item::class.java) }
                        itemList.clear()
                        itemList.addAll(items)
                        itemListAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "No data available in the collection",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@MainActivity,
                        "Error getting data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(
                this@MainActivity,
                "User not logged in. Data cannot be retrieved.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkInternetConnection() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected

        if (isConnected) {
            Toast.makeText(
                this,
                "Your internet is turned on. Now you can use the app.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Your internet is turned off. Please turn on your internet for using the app.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}