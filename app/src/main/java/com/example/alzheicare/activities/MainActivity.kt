package com.example.alzheicare.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alzheicare.R
import com.example.alzheicare.adapters.ItemListAdapter
import com.example.alzheicare.data_models.Item
import com.example.alzheicare.databinding.ActivityMainBinding
import com.example.alzheicare.databinding.DialogNoWifiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var itemList: MutableList<Item>
    private lateinit var filteredItemList: MutableList<Item>
    private var itemsListener: ListenerRegistration? = null

    private var noWifiDialog: AlertDialog? = null
    private var isAddItemLayoutVisible = false
    private var isSearchItemVisible = false

    companion object {
        private const val COLLECTION_USER_INVENTORIES = "user_inventories"
        private const val COLLECTION_ITEMS = "items"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_ITEM_NAME = "itemName"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_ITEM_TYPE = "itemType"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_UPDATED_AT = "updatedAt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser ?: return

        setupRecyclerView()
        setupSwipeRefresh()
        setEventHandlers()
        checkInternetConnection()
    }

    override fun onStart() {
        super.onStart()
        setupFirestoreListener()
    }

    override fun onResume() {
        super.onResume()
        checkInternetConnection()
    }

    override fun onStop() {
        super.onStop()
        itemsListener?.remove()
        noWifiDialog?.dismiss()
    }

    // -------------------- EVENT HANDLERS --------------------

    private fun setEventHandlers() {
        binding.addItemButton.setOnClickListener {
            if (isInternetAvailable()) toggleAddItemLayout()
            else showNoWifiDialog()
        }

        binding.searchButton.setOnClickListener {
            if (isInternetAvailable()) toggleSearchItemLayout()
            else showNoWifiDialog()
        }

        binding.itemSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItemList(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearButton.setOnClickListener {
            binding.itemSearch.text?.clear()
            hideSearchItemLayout()
        }

        binding.profileImg.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnCloseAddItem.setOnClickListener {
            hideAddItemLayout()
        }

        binding.sendButton.setOnClickListener {
            if (isInternetAvailable()) addItemToDatabase()
            else showNoWifiDialog()
        }

        // ✅ Setup radio buttons manually (because they are inside CardViews)
        setupRadioButtons()
    }

    // -------------------- RADIO BUTTON FIX --------------------

    private fun setupRadioButtons() {
        binding.radioObject.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioContainer.isChecked = false
            }
        }

        binding.radioContainer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioObject.isChecked = false
            }
        }
    }

    private fun getSelectedItemTypeWithValidation(): String? {
        return when {
            binding.radioObject.isChecked -> "OBJECT"
            binding.radioContainer.isChecked -> "CONTAINER"
            else -> null
        }
    }

    private fun validateRadioButtons(): Boolean {
        return when {
            binding.radioObject.isChecked -> true
            binding.radioContainer.isChecked -> true
            else -> {
                Toast.makeText(this, "Please select item type", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    // -------------------- ADD ITEM --------------------

    private fun addItemToDatabase() {
        val itemNameText = binding.itemName.text.toString().trim()
        val descriptionText = binding.description.text.toString().trim()

        if (!validateRadioButtons()) return
        val itemType = getSelectedItemTypeWithValidation()

        if (itemNameText.isEmpty() || descriptionText.isEmpty()) {
            Toast.makeText(this, "Item name and description cannot be empty", Toast.LENGTH_LONG).show()
            return
        }

        if (itemNameText.length < 2) {
            Toast.makeText(this, "Item name should be at least 2 characters", Toast.LENGTH_LONG).show()
            return
        }

        if (itemType == null) {
            Toast.makeText(this, "Please select item type", Toast.LENGTH_LONG).show()
            return
        }

        if (!isInternetAvailable()) {
            showNoWifiDialog()
            return
        }

        setLoadingState(true)

        val item = hashMapOf(
            FIELD_USER_ID to currentUser.uid,
            FIELD_ITEM_NAME to itemNameText,
            FIELD_DESCRIPTION to descriptionText,
            FIELD_ITEM_TYPE to itemType,
            FIELD_CREATED_AT to System.currentTimeMillis(),
            FIELD_UPDATED_AT to System.currentTimeMillis()
        )

        firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)
            .collection(COLLECTION_ITEMS)
            .add(item)
            .addOnSuccessListener {
                setLoadingState(false)
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                hideAddItemLayout()
                refreshData()
            }
            .addOnFailureListener { e ->
                setLoadingState(false)
                if (!isInternetAvailable()) showNoWifiDialog()
                else Toast.makeText(this, "Error adding item: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.sendButton.isEnabled = !isLoading
        binding.sendButton.text = if (isLoading) "Adding..." else "Add to Inventory"
    }

    private fun clearAddItemForm() {
        binding.itemName.text?.clear()
        binding.description.text?.clear()
        binding.radioObject.isChecked = false
        binding.radioContainer.isChecked = false
    }

    // -------------------- RECYCLER & FIRESTORE --------------------

    private fun setupRecyclerView() {
        itemList = mutableListOf()
        filteredItemList = mutableListOf()
        itemListAdapter = ItemListAdapter(filteredItemList)
        binding.itemsListView.apply {
            adapter = itemListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupFirestoreListener() {
        if (!isInternetAvailable()) {
            binding.swipeRefreshLayout.isRefreshing = false
            return
        }

        itemsListener?.remove()
        itemsListener = firestore.collection(COLLECTION_USER_INVENTORIES)
            .document(currentUser.uid)
            .collection(COLLECTION_ITEMS)
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.swipeRefreshLayout.isRefreshing = false

                if (error != null) {
                    if (!isInternetAvailable()) showNoWifiDialog()
                    else Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        val itemName = doc.getString(FIELD_ITEM_NAME) ?: ""
                        val desc = doc.getString(FIELD_DESCRIPTION) ?: ""
                        val type = doc.getString(FIELD_ITEM_TYPE) ?: ""
                        val createdAt = doc.getLong(FIELD_CREATED_AT) ?: System.currentTimeMillis()
                        Item(
                            itemName = itemName,
                            description = desc,
                            itemType = type,
                            documentId = doc.id,
                            createdAt = createdAt,
                            itemId = doc.id
                        )
                    }
                    itemList.clear()
                    itemList.addAll(items)
                    filteredItemList.clear()
                    filteredItemList.addAll(itemList)
                    itemListAdapter.notifyDataSetChanged()
                }
            }
    }
    private fun refreshData() {
        itemsListener?.remove()
        setupFirestoreListener()
    }

    // -------------------- SEARCH --------------------

    @SuppressLint("NotifyDataSetChanged")
    private fun filterItemList(searchText: String) {
        val filteredItems = itemList.filter {
            it.itemName.contains(searchText, true) ||
                    it.description.contains(searchText, true) ||
                    it.itemType.contains(searchText, true)
        }
        filteredItemList.clear()
        filteredItemList.addAll(filteredItems)
        itemListAdapter.notifyDataSetChanged()
    }

    // -------------------- UI LAYOUT TOGGLES --------------------

    private fun toggleAddItemLayout() {
        if (binding.addItemLayout.isGone) {
            binding.addItemLayout.visibility = View.VISIBLE
            binding.itemsListView.visibility = View.GONE
            isAddItemLayoutVisible = true
        } else {
            hideAddItemLayout()
        }
        binding.searchItemLayout.visibility = View.GONE
        isSearchItemVisible = false
    }

    private fun hideAddItemLayout() {
        binding.addItemLayout.visibility = View.GONE
        binding.itemsListView.visibility = View.VISIBLE
        isAddItemLayoutVisible = false
        clearAddItemForm()
    }

    private fun toggleSearchItemLayout() {
        if (binding.searchItemLayout.isGone) {
            binding.searchItemLayout.visibility = View.VISIBLE
            isSearchItemVisible = true
            binding.itemSearch.requestFocus()
        } else {
            hideSearchItemLayout() // Use the new method here too
        }
        binding.addItemLayout.visibility = View.GONE
        isAddItemLayoutVisible = false
    }

    // Add this new method to hide search layout
    private fun hideSearchItemLayout() {
        binding.searchItemLayout.visibility = View.GONE
        isSearchItemVisible = false
        binding.itemSearch.text?.clear()
        // Ensure the items list is visible when search is hidden
        binding.itemsListView.visibility = View.VISIBLE
    }

    // -------------------- INTERNET HANDLING --------------------

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


    @SuppressLint("ObsoleteSdkInt")
    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val nc = cm.getNetworkCapabilities(network) ?: return false
            return nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    || nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun checkInternetConnection() {
        if (!isInternetAvailable()) showNoWifiDialog()
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
        setupFirestoreListener()
        if (isAddItemLayoutVisible && !isInternetAvailable()) {
            binding.addItemLayout.visibility = View.VISIBLE
        }
    }
}