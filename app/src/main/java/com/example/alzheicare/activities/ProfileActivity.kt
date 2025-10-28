package com.example.alzheicare.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alzheicare.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    companion object {
        private const val TAG = "ProfileActivity"
        private const val COLLECTION_USER_INVENTORIES = "user_inventories"
        private const val COLLECTION_ITEMS = "items"
        private const val COLLECTION_ITEM_CONTAINERS = "item_containers"
        private const val FIELD_ITEM_TYPE = "itemType"
        private const val FIELD_ITEM_NAME = "itemName"
        private const val FIELD_CONTAINER_NAME = "containerName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setClickListeners()
        loadUserData()
        loadComprehensiveStatistics()
    }

    private fun initializeFirebase() {
        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun setClickListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun loadUserData() {
        // Set user email
        binding.userMail.text = currentUser.email ?: "No email"

        // Get user name from displayName or extract from email
        val displayName = currentUser.displayName
        val userName = if (!displayName.isNullOrEmpty()) {
            // Use display name if available
            displayName
        } else {
            // Extract username from email (part before @)
            currentUser.email?.substringBefore("@")?.replace(".", " ")?.split(" ")?.joinToString(" ") {
                it.replaceFirstChar { char -> char.uppercase() }
            } ?: "User"
        }

        binding.userName.text = userName

        Log.d(TAG, "User loaded from Auth - Name: $userName, Email: ${currentUser.email}")
        Log.d(TAG, "User UID: ${currentUser.uid}")
    }

    private fun loadComprehensiveStatistics() {
        val userId = currentUser.uid
        Log.d(TAG, "Loading comprehensive statistics for user: $userId")

        // Get all items to count objects and containers
        firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(userId)
            .collection(COLLECTION_ITEMS)
            .get()
            .addOnSuccessListener { itemsDocuments ->
                val totalElements = itemsDocuments.size()
                Log.d(TAG, "Total elements found: $totalElements")

                var totalObjects = 0
                var totalContainers = 0
                val containerMap = mutableMapOf<String, String>() // containerId to containerName

                // Count objects and containers, and collect container info
                for (document in itemsDocuments) {
                    val itemType = document.getString(FIELD_ITEM_TYPE)
                    val itemName = document.getString(FIELD_ITEM_NAME) ?: "Unnamed"
                    when (itemType?.uppercase()) {
                        "OBJECT" -> totalObjects++
                        "CONTAINER" -> {
                            totalContainers++
                            containerMap[document.id] = itemName
                        }
                    }
                }

                Log.d(TAG, "Basic counts - Objects: $totalObjects, Containers: $totalContainers")

                // Update basic counts first
                updateBasicCounts(totalElements, totalObjects, totalContainers)

                // Now load detailed container relationships
                if (containerMap.isNotEmpty()) {
                    loadDetailedContainerStatistics(containerMap)
                } else {
                    // No containers, set detailed counts to zero
                    updateDetailedContainerCounts(0, 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting items: ${e.message}", e)
                Toast.makeText(this, "Error loading statistics", Toast.LENGTH_LONG).show()
                setDefaultCountValues()
            }
    }

    private fun loadDetailedContainerStatistics(containerMap: Map<String, String>) {
        val userId = currentUser.uid
        var containersWithItems = 0
        var containersWithContainers = 0
        var processedContainers = 0

        Log.d(TAG, "Analyzing ${containerMap.size} containers for detailed statistics")

        // Check each container to see what it contains
        for ((containerId, containerName) in containerMap) {
            firestore.collection(COLLECTION_USER_INVENTORIES)
                .document(userId)
                .collection(COLLECTION_ITEM_CONTAINERS)
                .whereEqualTo(FIELD_CONTAINER_NAME, containerName)
                .get()
                .addOnSuccessListener { containerRelations ->
                    processedContainers++

                    if (!containerRelations.isEmpty) {
                        // This container has items inside it
                        containersWithItems++

                        var hasContainers = false
                        var processedItems = 0

                        for (relationDoc in containerRelations) {
                            val containedItemId = relationDoc.id

                            // Check if the contained item is a container
                            firestore.collection(COLLECTION_USER_INVENTORIES)
                                .document(userId)
                                .collection(COLLECTION_ITEMS)
                                .document(containedItemId)
                                .get()
                                .addOnSuccessListener { itemDoc ->
                                    processedItems++

                                    if (itemDoc.exists()) {
                                        val itemType = itemDoc.getString(FIELD_ITEM_TYPE)
                                        if (itemType?.uppercase() == "CONTAINER") {
                                            hasContainers = true
                                        }
                                    }

                                    // Check if we've processed all items for this container
                                    if (processedItems == containerRelations.size()) {
                                        if (hasContainers) {
                                            containersWithContainers++
                                        }

                                        // Check if we've processed all containers
                                        if (processedContainers == containerMap.size) {
                                            updateDetailedContainerCounts(containersWithItems, containersWithContainers)
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error checking contained item: ${e.message}", e)
                                    processedItems++

                                    if (processedItems == containerRelations.size()) {
                                        if (hasContainers) {
                                            containersWithContainers++
                                        }

                                        if (processedContainers == containerMap.size) {
                                            updateDetailedContainerCounts(containersWithItems, containersWithContainers)
                                        }
                                    }
                                }
                        }

                        // If no relations, check completion
                        if (containerRelations.isEmpty()) {
                            if (processedContainers == containerMap.size) {
                                updateDetailedContainerCounts(containersWithItems, containersWithContainers)
                            }
                        }
                    } else {
                        // No items in this container
                        if (processedContainers == containerMap.size) {
                            updateDetailedContainerCounts(containersWithItems, containersWithContainers)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading container relationships: ${e.message}", e)
                    processedContainers++
                    if (processedContainers == containerMap.size) {
                        updateDetailedContainerCounts(containersWithItems, containersWithContainers)
                    }
                }
        }
    }

    private fun updateBasicCounts(totalElements: Int, totalObjects: Int, totalContainers: Int) {
        runOnUiThread {
            binding.elementsCount.text = totalElements.toString()
            binding.objectsCount.text = totalObjects.toString()
            binding.containersCount.text = totalContainers.toString()
        }
    }

    private fun updateDetailedContainerCounts(containersWithItems: Int, containersWithContainers: Int) {
        runOnUiThread {
            binding.containersWithItemsCount.text = containersWithItems.toString()
            binding.nestedContainersCount.text = containersWithContainers.toString()

            // Show summary in log
            showStatisticsSummary(containersWithItems, containersWithContainers)
        }
    }

    private fun showStatisticsSummary(containersWithItems: Int, containersWithContainers: Int) {
        val summary = """
            Detailed Container Analysis:
            • Containers containing items: $containersWithItems
            • Containers containing other containers: $containersWithContainers
        """.trimIndent()

        Log.d(TAG, summary)
        Toast.makeText(this, "Statistics loaded successfully", Toast.LENGTH_SHORT).show()
    }

    private fun setDefaultCountValues() {
        runOnUiThread {
            binding.elementsCount.text = "0"
            binding.objectsCount.text = "0"
            binding.containersCount.text = "0"
            binding.containersWithItemsCount.text = "0"
            binding.nestedContainersCount.text = "0"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh statistics when activity resumes
        loadComprehensiveStatistics()
    }
}