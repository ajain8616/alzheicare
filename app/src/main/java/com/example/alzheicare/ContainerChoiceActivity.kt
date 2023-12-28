package com.example.alzheicare

import android.content.Intent
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
    private lateinit var containerListView: RecyclerView
    private lateinit var linearLayout: LinearLayout
    private lateinit var containerListAdapter: ContainerListAdapter
    private lateinit var containerList: MutableList<Item>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

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
            setContainerInItem()
            val intentForDataSetActivity = Intent(this@ContainerChoiceActivity, DataSetDetailsActivity::class.java)
            startActivity(intentForDataSetActivity)
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

    private fun setContainerInItem() {
        // Retrieve item name and selected container name
        val itemName = objectTxtView.text.toString()
        val containerName = containerTxtView.text.toString()

        // Check if the container name is not empty
        if (containerName.isNotEmpty()) {
            // Save data to the "Item_In_Container" collection
            val userId = currentUser?.uid
            if (userId != null) {
                val collectionName = "Item_In_Container"
                val newItemInContainerRef = database.child(collectionName).child(userId).child(itemName)

                // Save the selected container name
                newItemInContainerRef.child("containerName").setValue(containerName)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@ContainerChoiceActivity,
                            "Container set for item successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@ContainerChoiceActivity,
                            "Error setting container for item: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            } else {
                Toast.makeText(
                    this@ContainerChoiceActivity,
                    "User not logged in. Data cannot be saved.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this@ContainerChoiceActivity,
                "Please select a container",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}