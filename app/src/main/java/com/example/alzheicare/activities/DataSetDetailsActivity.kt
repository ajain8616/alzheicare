package com.example.alzheicare.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.alzheicare.R
import com.example.alzheicare.databinding.ActivityDataSetDetailsBinding
import com.example.alzheicare.databinding.DialogNoWifiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataSetDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataSetDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore

    private var noWifiDialog: AlertDialog? = null
    private var isMenuVisible = false
    private var originalItemId: String? = null
    private var originalItemName: String? = null

    // Collection names matching MainActivity structure
    companion object {
        private const val TAG = "DataSetDetailsActivity"
        private const val COLLECTION_USER_INVENTORIES = "user_inventories"
        private const val COLLECTION_ITEMS = "items"
        private const val COLLECTION_ITEM_CONTAINERS = "item_containers"
        private const val COLLECTION_UPDATED_ITEMS = "updated_items"

        private const val FIELD_ITEM_NAME = "itemName"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_ITEM_TYPE = "itemType"
        private const val FIELD_CONTAINER_NAME = "containerName"
        private const val FIELD_UPDATED_AT = "updatedAt"
        private const val CONTAINER_CHOICE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataSetDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get the original item ID and name from intent
        originalItemId = intent.getStringExtra("itemId")
        originalItemName = intent.getStringExtra("itemName")

        Log.d(TAG, "onCreate: Item ID = $originalItemId, Item Name = $originalItemName")
        Log.d(TAG, "onCreate: Current User ID = ${currentUser.uid}")

        setEventHandlers()
        displayData()
        checkInternetConnection()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity resumed")
        checkInternetConnection()
    }

    override fun onStop() {
        super.onStop()
        noWifiDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTAINER_CHOICE_REQUEST) {
            when (resultCode) {
                RESULT_OK -> {
                    val containerName = data?.getStringExtra("containerName")
                    Toast.makeText(this, "Container set: $containerName", Toast.LENGTH_SHORT).show()
                    loadCurrentItemContainerInfo()
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "Container selection cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // -------------------- EVENT HANDLERS --------------------

    private fun setEventHandlers() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.menuOptionsButton.setOnClickListener {
            if (isInternetAvailable()) toggleMenuOptions()
            else showNoWifiDialog()
        }

        binding.menuEditOption.setOnClickListener {
            Log.d(TAG, "Edit option clicked")
            if (isInternetAvailable()) showEditForm()
            else showNoWifiDialog()
        }

        binding.menuDeleteOption.setOnClickListener {
            Log.d(TAG, "Delete option clicked")
            if (isInternetAvailable()) showDeleteConfirmation()
            else showNoWifiDialog()
        }

        binding.menuSetContainerOption.setOnClickListener {
            if (isInternetAvailable()) navigateToContainerChoice()
            else showNoWifiDialog()
        }

        binding.saveChangesButton.setOnClickListener {
            if (isInternetAvailable()) updateItemDetails()
            else showNoWifiDialog()
        }

        binding.cancelButton.setOnClickListener {
            Log.d(TAG, "Cancel button clicked")
            hideEditForm()
        }

        setupRadioButtons()

        binding.blurOverlay.setOnClickListener {
            Log.d(TAG, "Blur overlay clicked")
            hideMenuOptions()
        }
    }

    private fun setupRadioButtons() {
        binding.radioItem.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d(TAG, "Radio Item selected")
                binding.radioContainer.isChecked = false
            }
        }

        binding.radioContainer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d(TAG, "Radio Container selected")
                binding.radioItem.isChecked = false
            }
        }
    }

    private fun toggleMenuOptions() {
        isMenuVisible = !isMenuVisible
        binding.menuOptionsLayout.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
        binding.blurOverlay.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
    }

    private fun hideMenuOptions() {
        isMenuVisible = false
        binding.menuOptionsLayout.visibility = View.GONE
        binding.blurOverlay.visibility = View.GONE
    }

    // -------------------- DATA DISPLAY --------------------

    private fun displayData() {
        val itemName = intent.getStringExtra("itemName")
        val description = intent.getStringExtra("description")
        val itemType = intent.getStringExtra("itemType")
        val createdAt = intent.getLongExtra("createdAt", System.currentTimeMillis())

        Log.d(TAG, "displayData: Displaying item - Name: $itemName, Type: $itemType")

        binding.itemNameView.text = itemName
        binding.descriptionView.text = description
        binding.currentItemType.text = itemType
        binding.itemTypeBadge.text = itemType

        // Set created time
        binding.createdTimeView.text = getFormattedTime(createdAt)

        updateItemTypeUI(binding.itemTypeView, itemType, binding.iconContainer)

        // Pre-fill edit form
        binding.editItemName.setText(itemName)
        binding.editDescription.setText(description)
        preSelectRadioButton(itemType)

        // Load container info for the current item
        loadCurrentItemContainerInfo()
    }

    private fun loadCurrentItemContainerInfo() {
        val itemId = originalItemId ?: return

        firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)
            .collection(COLLECTION_ITEM_CONTAINERS)
            .document(itemId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val containerName = document.getString(FIELD_CONTAINER_NAME)
                    if (!containerName.isNullOrEmpty()) {
                        binding.currentItemContainerName.text = "In: $containerName"
                        binding.currentItemContainerName.visibility = View.VISIBLE
                    } else {
                        binding.currentItemContainerName.visibility = View.GONE
                    }
                } else {
                    binding.currentItemContainerName.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading container info: ${e.message}")
                binding.currentItemContainerName.visibility = View.GONE
            }
    }

    private fun getFormattedTime(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val now = Date()
            val diff = now.time - timestamp

            when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> {
                    val minutes = (diff / (60 * 1000)).toInt()
                    "$minutes min ago"
                }
                diff < 24 * 60 * 60 * 1000 -> {
                    val hours = (diff / (60 * 60 * 1000)).toInt()
                    "$hours hour${if (hours > 1) "s" else ""} ago"
                }
                diff < 7 * 24 * 60 * 60 * 1000 -> {
                    val days = (diff / (24 * 60 * 60 * 1000)).toInt()
                    "$days day${if (days > 1) "s" else ""} ago"
                }
                else -> {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(date)
                }
            }
        } catch (e: Exception) {
            "Unknown time"
        }
    }

    private fun updateItemTypeUI(imageView: android.widget.ImageView, itemType: String?, iconContainer: View) {
        when (itemType?.uppercase()) {
            "OBJECT" -> {
                imageView.setImageResource(R.drawable.ic_objects)
                imageView.clearColorFilter()
                binding.itemTypeBadge.setBackgroundResource(R.drawable.badge_object)
                binding.itemTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                iconContainer.setBackgroundResource(R.drawable.icon_gradient_object)
                Log.d(TAG, "updateItemTypeUI: Object type set")
            }
            "CONTAINER" -> {
                imageView.setImageResource(R.drawable.ic_container)
                imageView.clearColorFilter()
                binding.itemTypeBadge.setBackgroundResource(R.drawable.badge_container)
                binding.itemTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                iconContainer.setBackgroundResource(R.drawable.icon_gradient_container)
                Log.d(TAG, "updateItemTypeUI: Container type set")
            }
            else -> {
                imageView.setImageResource(R.drawable.ic_block)
                imageView.clearColorFilter()
                binding.itemTypeBadge.setBackgroundResource(R.drawable.badge_background)
                binding.itemTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                iconContainer.setBackgroundResource(R.drawable.icon_gradient_background)
                Log.d(TAG, "updateItemTypeUI: Default type set")
            }
        }
    }

    private fun preSelectRadioButton(itemType: String?) {
        when (itemType?.uppercase()) {
            "OBJECT" -> {
                binding.radioItem.isChecked = true
                Log.d(TAG, "preSelectRadioButton: Object radio selected")
            }
            "CONTAINER" -> {
                binding.radioContainer.isChecked = true
                Log.d(TAG, "preSelectRadioButton: Container radio selected")
            }
        }
    }

    // -------------------- FIREBASE OPERATIONS --------------------


    @SuppressLint("SetTextI18n")
    private fun updateItemDetails() {
        val itemId = originalItemId ?: return
        val newItemName = binding.editItemName.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()

        Log.d(TAG, "updateItemDetails: Starting update for item ID: $itemId")
        Log.d(TAG, "updateItemDetails: New name: '$newItemName', Description: '$description'")

        if (newItemName.isEmpty() || description.isEmpty()) {
            Log.w(TAG, "updateItemDetails: Validation failed - empty fields")
            Toast.makeText(this, "Item name and description cannot be empty", Toast.LENGTH_LONG).show()
            return
        }

        if (newItemName.length < 2) {
            Log.w(TAG, "updateItemDetails: Validation failed - name too short")
            Toast.makeText(this, "Item name should be at least 2 characters", Toast.LENGTH_LONG).show()
            return
        }

        val itemType = when {
            binding.radioItem.isChecked -> {
                Log.d(TAG, "updateItemDetails: Item type selected: OBJECT")
                "OBJECT"
            }
            binding.radioContainer.isChecked -> {
                Log.d(TAG, "updateItemDetails: Item type selected: CONTAINER")
                "CONTAINER"
            }
            else -> {
                Log.w(TAG, "updateItemDetails: Validation failed - no item type selected")
                Toast.makeText(this, "Please select item type", Toast.LENGTH_LONG).show()
                return
            }
        }

        setLoadingState(true)

        val updatedData = hashMapOf<String, Any>(
            FIELD_ITEM_NAME to newItemName,
            FIELD_DESCRIPTION to description,
            FIELD_ITEM_TYPE to itemType,
            FIELD_UPDATED_AT to System.currentTimeMillis()
        )

        Log.d(TAG, "updateItemDetails: Update data prepared: $updatedData")

        // Update the document using item ID
        firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)
            .collection(COLLECTION_ITEMS)
            .document(itemId)
            .update(updatedData)
            .addOnSuccessListener {
                Log.d(TAG, "updateItemDetails: Successfully updated main item document")

                // Save to updated_items collection for history
                firestore.collection(COLLECTION_USER_INVENTORIES)
                    .document(currentUser.uid)
                    .collection(COLLECTION_UPDATED_ITEMS)
                    .document(itemId)
                    .set(updatedData)
                    .addOnSuccessListener {
                        Log.d(TAG, "updateItemDetails: Successfully saved to updated_items")
                        setLoadingState(false)
                        showUpdateSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "updateItemDetails: Failed to save to updated_items", e)
                        setLoadingState(false)
                        handleUpdateError(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateItemDetails: Failed to update main item document", e)
                setLoadingState(false)
                handleUpdateError(e)
            }
    }

    private fun deleteItem() {
        val itemId = originalItemId ?: return

        Log.d(TAG, "deleteItem: Starting delete for item ID: $itemId")

        // Delete from all collections using item ID
        val userInventoryRef = firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)

        // Delete from items collection
        userInventoryRef.collection(COLLECTION_ITEMS)
            .document(itemId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteItem: Successfully deleted from items collection")

                // Delete from item_containers collection
                userInventoryRef.collection(COLLECTION_ITEM_CONTAINERS)
                    .document(itemId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "deleteItem: Successfully deleted from item_containers collection")

                        // Delete from updated_items collection
                        userInventoryRef.collection(COLLECTION_UPDATED_ITEMS)
                            .document(itemId)
                            .delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "deleteItem: Successfully deleted from updated_items collection")
                                Log.d(TAG, "deleteItem: All collections deleted successfully")
                                showDeleteSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "deleteItem: Failed to delete from updated_items, but continuing", e)
                                // Even if this fails, we still show success since main item is deleted
                                showDeleteSuccess()
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "deleteItem: Failed to delete from item_containers, but continuing", e)
                        // Even if this fails, we still show success since main item is deleted
                        showDeleteSuccess()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "deleteItem: Failed to delete from items collection", e)
                if (!isInternetAvailable()) showNoWifiDialog()
                else Toast.makeText(this, "Error deleting item: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // -------------------- UI STATE MANAGEMENT --------------------

    private fun showEditForm() {
        Log.d(TAG, "showEditForm: Showing edit form")
        binding.editFormCard.visibility = View.VISIBLE
        binding.currentItemCard.visibility = View.GONE
        hideMenuOptions()
    }

    private fun hideEditForm() {
        Log.d(TAG, "hideEditForm: Hiding edit form")
        binding.editFormCard.visibility = View.GONE
        binding.currentItemCard.visibility = View.VISIBLE
    }

    private fun showDeleteConfirmation() {
        Log.d(TAG, "showDeleteConfirmation: Showing delete confirmation dialog")
        hideMenuOptions()
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                Log.d(TAG, "showDeleteConfirmation: User confirmed delete")
                deleteItem()
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.d(TAG, "showDeleteConfirmation: User cancelled delete")
            }
            .show()
    }

    private fun setLoadingState(isLoading: Boolean) {
        Log.d(TAG, "setLoadingState: Loading state = $isLoading")
        binding.saveChangesButton.isEnabled = !isLoading
        binding.saveChangesButton.text = if (isLoading) "Saving..." else "Save Changes"
    }

    private fun hideAllUIElements() {
        Log.d(TAG, "hideAllUIElements: Hiding all UI elements")
        // Hide all UI elements except the background
        binding.appBarLayout.visibility = View.GONE
        binding.menuOptionsLayout.visibility = View.GONE
        binding.currentItemCard.visibility = View.GONE
        binding.editFormCard.visibility = View.GONE
        binding.blurOverlay.visibility = View.GONE
    }

    private fun showUpdateSuccess() {
        Log.d(TAG, "showUpdateSuccess: Showing update success animation")
        // Hide all UI and show only animation
        hideAllUIElements()
        binding.updateMessage.visibility = View.VISIBLE
        binding.updateMessage.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "showUpdateSuccess: Navigating back to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun showDeleteSuccess() {
        Log.d(TAG, "showDeleteSuccess: Showing delete success animation")
        // Hide all UI and show only animation
        hideAllUIElements()
        binding.deleteMessage.visibility = View.VISIBLE
        binding.deleteMessage.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "showDeleteSuccess: Navigating back to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun navigateToContainerChoice() {
        hideMenuOptions()
        val intent = Intent(this, ContainerChoiceActivity::class.java)
        intent.putExtra("itemId", originalItemId)
        intent.putExtra("itemName", originalItemName)
        startActivityForResult(intent, CONTAINER_CHOICE_REQUEST)
    }

    // -------------------- ERROR HANDLING --------------------

    private fun handleUpdateError(e: Exception) {
        Log.e(TAG, "handleUpdateError: Update failed", e)
        if (!isInternetAvailable()) {
            showNoWifiDialog()
        } else {
            val errorMessage = "Error updating item: ${e.message}"
            Log.e(TAG, errorMessage)
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    // -------------------- INTERNET HANDLING --------------------

    @SuppressLint("ObsoleteSdkInt")
    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

    private fun checkInternetConnection() {
        val isConnected = isInternetAvailable()
        Log.d(TAG, "checkInternetConnection: Internet available = $isConnected")
        if (!isConnected) showNoWifiDialog()
        else noWifiDialog?.dismiss()
    }

    private fun showNoWifiDialog() {
        noWifiDialog?.dismiss()
        val dialogBinding = DialogNoWifiBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)

        noWifiDialog = builder.create()
        noWifiDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        noWifiDialog?.show()

        // Modified: Open WiFi settings when action button is clicked
        dialogBinding.customButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
            noWifiDialog?.dismiss()
            retryPendingOperations()

        }

        // Close dialog when cross icon is clicked
        dialogBinding.crossIcon.setOnClickListener {
            noWifiDialog?.dismiss()
            retryPendingOperations()

        }
    }

    private fun retryPendingOperations() {
        Log.d(TAG, "retryPendingOperations: Retrying pending operations")
        loadCurrentItemContainerInfo()
    }
}