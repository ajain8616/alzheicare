package com.example.alzheicare

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ContainerChoiceActivity : AppCompatActivity(), ContainerListAdapter.OnItemClickListener {
    private lateinit var objectTxtView: TextView
    private lateinit var inTxtView: TextView
    private lateinit var containerTxtView: TextView
    private lateinit var submitButton: ImageButton
    private lateinit var containerChoice: LottieAnimationView
    private lateinit var containerListView: RecyclerView
    private lateinit var linearLayout: LinearLayout
    private lateinit var containerListAdapter: ContainerListAdapter
    private lateinit var containerList: MutableList<Item>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val selectedCollectionList: MutableList<Item> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_choice)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        findIdsOfElements()
        setEventHandlers()
        containerList = mutableListOf()
        containerListAdapter = ContainerListAdapter(containerList, this)
    }

    private fun findIdsOfElements() {
        objectTxtView = findViewById(R.id.objectTxtView)
        inTxtView = findViewById(R.id.inTxtView)
        containerTxtView = findViewById(R.id.containerTxtView)
        submitButton = findViewById(R.id.submitButton)
        containerChoice = findViewById(R.id.containerChoice)
        linearLayout = findViewById(R.id.linearLayout)
        containerListView = findViewById(R.id.containerListView)
    }

    private fun setEventHandlers() {
        // Retrieve itemName and containerName from intent
        val itemNameFromIntent = intent.getStringExtra("itemName")
        // Set the text of objectTxtView and containerTxtView with the retrieved values
        objectTxtView.text = itemNameFromIntent

        if (currentUser != null) {
            getDataFromFirebase()
        }

        submitButton.setOnClickListener {

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
                                .filter { it.itemType == "CONTAINER" }
                        containerList.clear()
                        containerList.addAll(items)
                        containerListAdapter.notifyDataSetChanged()
                        containerListView.layoutManager = GridLayoutManager(this, 2)
                        containerListView.adapter = containerListAdapter
                    } else {
                        Toast.makeText(
                            this@ContainerChoiceActivity,
                            "No data available in the collection",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@ContainerChoiceActivity,
                        "Error getting data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(
                this@ContainerChoiceActivity,
                "User not logged in. Data cannot be retrieved.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onItemClick(container: Item) {
        // Handle item click, update the containerTxtView with the selected container's name
        containerTxtView.text = container.itemName
    }




}