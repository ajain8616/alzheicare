package com.example.alzheicare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContainerCollectionsActivity : AppCompatActivity() {
    private lateinit var containerSelectedName: TextView
    private lateinit var containerCollectionListView: RecyclerView
    private lateinit var containerCollectionList: MutableList<Item>
    private lateinit var containerCollectionAdapter: ContainerCollectionAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_collections)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        currentUser = auth.currentUser!!

        setEventHandler()
        setupRecyclerView()
    }

    private fun setEventHandler() {
        findElementsById()
        val containerName = intent.getStringExtra("selectedContainerName")
        containerSelectedName.text = containerName
        if (currentUser != null) {
            getContainerCollection()
        }
    }

    private fun findElementsById() {
        containerSelectedName = findViewById(R.id.containerSelectedName)
        containerCollectionListView = findViewById(R.id.containerCollectionListView)
    }


    private fun setupRecyclerView() {
        containerCollectionList = mutableListOf()
        containerCollectionAdapter = ContainerCollectionAdapter(this, containerCollectionList)
        containerCollectionListView.adapter = containerCollectionAdapter
        containerCollectionListView.layoutManager = GridLayoutManager(this,2)
    }

    private fun getContainerCollection() {
        val userId = currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val collectionName = "Item_In_Container"
        if (userId != null) {
            database.child(collectionName).child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (itemSnapshot in dataSnapshot.children) {
                            val itemName =itemSnapshot.key
                            val containerName = itemSnapshot.child("containerName").value.toString()
                            val container = Item(containerName) // Create an Item object with the containerName
                            containerCollectionList.add(container) // Add the item to the list
                            Log.d("ContainerCollection","Item: $itemName , Container: $containerName")
                        }
                        containerCollectionAdapter.notifyDataSetChanged() // Notify the adapter of the data change
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error
                    }
                })
        }
    }

}
