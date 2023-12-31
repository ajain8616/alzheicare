package com.example.alzheicare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
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
    private val containerCollectionList: MutableList<Item> = mutableListOf()
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
        containerCollectionAdapter = ContainerCollectionAdapter(this, containerCollectionList)
        containerCollectionListView.adapter = containerCollectionAdapter
        containerCollectionListView.layoutManager = GridLayoutManager(this,2)
    }


    private fun getContainerCollection() {
        val userId = currentUser.uid
        val itemNameExtra = intent.getStringExtra("itemName")
        val database = FirebaseDatabase.getInstance().reference
        val collectionName = "Item_In_Container"

        if (userId != null) {
            database.child(collectionName).child(userId).child(itemNameExtra!!).get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        for (containerSnapshot in dataSnapshot.children) {
                            val containerName = containerSnapshot.child("containerName").value.toString()
                            // Assuming Item class has a constructor that takes containerName as a parameter
                            val item = Item(containerName)
                            containerCollectionList.add(item)
                        }
                        containerCollectionAdapter.notifyDataSetChanged()
                    }
                }
        }
    }


}


