package com.example.alzheicare.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alzheicare.R
import com.example.alzheicare.adapters.ContainerListAdapter
import com.example.alzheicare.data_models.Item
import com.example.alzheicare.databinding.ActivityContainerChoiceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ContainerChoiceActivity : AppCompatActivity(), ContainerListAdapter.OnItemClickListener {

    private lateinit var binding: ActivityContainerChoiceBinding
    private lateinit var containerListAdapter: ContainerListAdapter
    private lateinit var containerList: MutableList<Item>
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private var itemsListener: ListenerRegistration? = null

    private var selectedContainer: Item? = null
    private var originalItemId: String? = null
    private var originalItemName: String? = null

    // Collection names matching your Firestore structure
    companion object {
        private const val TAG = "ContainerChoiceActivity"
        private const val COLLECTION_USER_INVENTORIES = "user_inventories"
        private const val COLLECTION_ITEMS = "items"
        private const val COLLECTION_ITEM_CONTAINERS = "item_containers"

        private const val FIELD_ITEM_NAME = "itemName"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_ITEM_TYPE = "itemType"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_CONTAINER_NAME = "containerName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContainerChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get intent data
        originalItemId = intent.getStringExtra("itemId")
        originalItemName = intent.getStringExtra("itemName")

        Log.d(TAG, "onCreate: Item ID = $originalItemId, Item Name = $originalItemName")
        Log.d(TAG, "onCreate: Current User ID = ${currentUser.uid}")

        initializeUI()
        setupRecyclerView()
        setupSwipeRefresh()
        setEventHandlers()
        setupFirestoreListener()
    }

    override fun onResume() {
        super.onResume()
        checkInternetConnection()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove Firestore listener to prevent memory leaks
        itemsListener?.remove()
    }

    private fun initializeUI() {
        // Set up the selected container layout
        binding.selectedContainerLayout.visibility = View.GONE

        // Initially disable submit button until container is selected
        binding.submitButton.isEnabled = false
        binding.submitButton.alpha = 0.5f

        // Update info text with item name
        binding.infoTitle.text = "Choose a Container"
        if (!originalItemName.isNullOrEmpty()) {
            binding.itemNameDisplay.text = "Item: $originalItemName"
            binding.itemNameDisplay.visibility = View.VISIBLE
        } else {
            binding.itemNameDisplay.visibility = View.GONE
        }
        binding.infoDescription.text = "Select a container to store your item. Only container-type items are shown below."
    }

    private fun setupRecyclerView() {
        containerList = mutableListOf()
        containerListAdapter = ContainerListAdapter(containerList, this, firestore, currentUser.uid)

        binding.containerListView.apply {
            adapter = containerListAdapter
            layoutManager = LinearLayoutManager(this@ContainerChoiceActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isInternetAvailable()) refreshData()
            else {
                binding.swipeRefreshLayout.isRefreshing = false
                showNoWifiDialog()
            }
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.colorOrange,
            R.color.colorBrightGreen
        )
    }

    private fun setEventHandlers() {
        // Back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // Submit button
        binding.submitButton.setOnClickListener {
            if (selectedContainer != null) {
                setContainerForItem()
            } else {
                Toast.makeText(this, "Please select a container first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFirestoreListener() {
        itemsListener?.remove()

        itemsListener = firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)
            .collection(COLLECTION_ITEMS)
            .whereEqualTo(FIELD_ITEM_TYPE, "CONTAINER")
            .addSnapshotListener { snapshot, error ->
                binding.swipeRefreshLayout.isRefreshing = false

                if (error != null) {
                    Log.e(TAG, "Error listening to containers: ${error.message}")
                    if (!isInternetAvailable()) {
                        showNoWifiDialog()
                    } else {
                        Toast.makeText(this, "Error loading containers: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val containers = snapshot.documents.mapNotNull { doc ->
                        try {
                            val itemName = doc.getString(FIELD_ITEM_NAME) ?: ""
                            val description = doc.getString(FIELD_DESCRIPTION) ?: ""
                            val itemType = doc.getString(FIELD_ITEM_TYPE) ?: ""
                            val createdAt = doc.getLong(FIELD_CREATED_AT) ?: System.currentTimeMillis()

                            Item(
                                itemName = itemName,
                                description = description,
                                itemType = itemType,
                                documentId = doc.id,
                                createdAt = createdAt,
                                itemId = doc.id
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing container document: ${e.message}")
                            null
                        }
                    }

                    Log.d(TAG, "Loaded ${containers.size} containers")

                    containerList.clear()
                    containerList.addAll(containers)
                    containerListAdapter.notifyDataSetChanged()

                    // Show empty state if no containers
                    if (containers.isEmpty()) {
                        binding.infoDescription.text = "No containers found. Create container-type items in your inventory first."
                    } else {
                        binding.infoDescription.text = "Select a container to store your item. ${containers.size} container(s) available."
                    }
                }
            }
    }

    private fun refreshData() {
        // Force refresh by reattaching the listener
        setupFirestoreListener()
    }

    private fun checkInternetConnection() {
        if (!isInternetAvailable()) {
            showNoWifiDialog()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val nc = cm.getNetworkCapabilities(network) ?: return false
            return nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun showNoWifiDialog() {
        Toast.makeText(this, "No internet connection. Please check your WiFi or mobile data.", Toast.LENGTH_LONG).show()
    }

    override fun onItemClick(container: Item) {
        Log.d(TAG, "Container selected: ${container.itemName}")
        selectedContainer = container
        updateSelectedContainerUI(container)
    }

    private fun updateSelectedContainerUI(container: Item) {
        binding.containerTxtView.text = container.itemName
        binding.selectedContainerLayout.visibility = View.VISIBLE

        // Enable submit button with full opacity
        binding.submitButton.isEnabled = true
        binding.submitButton.alpha = 1.0f

        // Show confirmation message
        Toast.makeText(this, "Selected: ${container.itemName}", Toast.LENGTH_SHORT).show()
    }

    private fun setContainerForItem() {
        val container = selectedContainer ?: return
        val itemId = originalItemId ?: return
        val itemName = originalItemName ?: return

        Log.d(TAG, "setContainerForItem: Setting container '${container.itemName}' for item '$itemName' (ID: $itemId)")

        // Check internet before proceeding
        if (!isInternetAvailable()) {
            showNoWifiDialog()
            return
        }

        // Disable button to prevent multiple clicks
        binding.submitButton.isEnabled = false
        binding.submitButton.text = "Saving..."
        binding.submitButton.alpha = 0.5f

        val containerData = hashMapOf<String, Any>(
            FIELD_CONTAINER_NAME to container.itemName,
            "containerId" to container.itemId,
            "itemId" to itemId,
            "itemName" to itemName,
            "userId" to currentUser.uid,
            "createdAt" to System.currentTimeMillis()
        )

        // Save to item_containers collection
        firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)
            .collection(COLLECTION_ITEM_CONTAINERS)
            .document(itemId) // Use itemId as document ID for easy lookup
            .set(containerData)
            .addOnSuccessListener {
                Log.d(TAG, "setContainerForItem: Successfully set container for item")

                // Also update the item document to mark it as having a container
                updateItemWithContainerInfo(itemId, container.itemName)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "setContainerForItem: Failed to set container", e)
                binding.submitButton.isEnabled = true
                binding.submitButton.text = "Confirm Selection"
                binding.submitButton.alpha = 1.0f

                if (!isInternetAvailable()) {
                    showNoWifiDialog()
                } else {
                    Toast.makeText(this, "Error setting container: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateItemWithContainerInfo(itemId: String, containerName: String) {
        val updateData = hashMapOf<String, Any>(
            "hasContainer" to true,
            "containerName" to containerName,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)
            .collection(COLLECTION_ITEMS)
            .document(itemId)
            .update(updateData)
            .addOnSuccessListener {
                Log.d(TAG, "updateItemWithContainerInfo: Successfully updated item with container info")
                showSuccessAndReturn()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateItemWithContainerInfo: Failed to update item, but container was set", e)
                // Still show success since the main operation (setting container) succeeded
                showSuccessAndReturn()
            }
    }

    private fun showSuccessAndReturn() {
        Toast.makeText(this, "Container set successfully!", Toast.LENGTH_LONG).show()

        val resultIntent = Intent().apply {
            putExtra("containerName", selectedContainer?.itemName)
            putExtra("containerId", selectedContainer?.itemId)
            putExtra("success", true)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        // Return with cancelled result
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}