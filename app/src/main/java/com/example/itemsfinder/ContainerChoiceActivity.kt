package com.example.itemsfinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ContainerChoiceActivity : AppCompatActivity() {
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
    private val selectedItemsList: MutableList<String> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_choice)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        setEventHandlers()
        containerList = mutableListOf()
        containerListAdapter = ContainerListAdapter(containerList)


    }

    private fun findIdsOfElements() {
        objectTxtView=findViewById(R.id.objectTxtView)
        inTxtView=findViewById(R.id.inTxtView)
        containerTxtView=findViewById(R.id.containerTxtView)
        submitButton=findViewById(R.id.submitButton)
        containerChoice=findViewById(R.id.containerChoice)
        linearLayout=findViewById(R.id.linearLayout)
        containerListView=findViewById(R.id.containerListView)
    }
    private fun setEventHandlers() {
        findIdsOfElements()

        // Retrieve itemName and containerName from intent
        val itemNameFromIntent = intent.getStringExtra("itemName")
        val containerNameFromIntent = intent.getStringExtra("containerName")

        // Set the text of objectTxtView and containerTxtView with the retrieved values
        objectTxtView.text = itemNameFromIntent
        containerTxtView.text = containerNameFromIntent

        if (currentUser != null) {
            getDataFromFirebase()
        }


        submitButton.setOnClickListener {
            // Add values to the selectedItemsList
            val objectText = objectTxtView.text.toString()
            val containerText = containerTxtView.text.toString()

            selectedItemsList.add(objectText)
            selectedItemsList.add(containerText)

            // Call the function to set the collection on Firebase
            setCollectionOnFirebase()
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
    private fun setCollectionOnFirebase() {
        val userId = currentUser?.uid
        val collectionName = "Collection_Of_Objects_Containers"

        if (userId != null) {
            val databaseReference = database.child(collectionName).child(userId)

            // Add the selected items to the database
            databaseReference.setValue(selectedItemsList)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@ContainerChoiceActivity,
                        "Data saved successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@ContainerChoiceActivity,
                        "Error saving data: ${e.message}",
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
    }

}