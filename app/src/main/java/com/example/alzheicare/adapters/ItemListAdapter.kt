package com.example.alzheicare.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.alzheicare.activities.DataSetDetailsActivity
import com.example.alzheicare.data_models.Item
import com.example.alzheicare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ItemListAdapter(private val itemList: List<Item>) :
    RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val description: TextView = itemView.findViewById(R.id.description)
        val itemType: TextView = itemView.findViewById(R.id.itemType)
        val itemTypeBadge: TextView = itemView.findViewById(R.id.itemTypeBadge)
        val itemTypeView: ImageView = itemView.findViewById(R.id.itemTypeView)
        val createdTime: TextView = itemView.findViewById(R.id.createdTime)
        val containerName: TextView = itemView.findViewById(R.id.containerName)
        val iconContainer: View = itemView.findViewById(R.id.iconContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        // Set basic item information
        holder.itemName.text = currentItem.itemName
        holder.description.text = currentItem.description
        holder.itemType.text = currentItem.itemType
        holder.itemTypeBadge.text = currentItem.itemType

        // Load container information if exists
        loadContainerInfo(currentItem.itemId, holder.containerName)

        // Set item click listener
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DataSetDetailsActivity::class.java)

            intent.putExtra("itemId", currentItem.itemId)
            intent.putExtra("itemName", currentItem.itemName)
            intent.putExtra("description", currentItem.description)
            intent.putExtra("itemType", currentItem.itemType)
            intent.putExtra("createdAt", currentItem.createdAt)

            Log.d("ItemListAdapter", "Opening details for:")
            Log.d("ItemListAdapter", "  - Item ID: ${currentItem.itemId}")
            Log.d("ItemListAdapter", "  - Item Name: ${currentItem.itemName}")
            Log.d("ItemListAdapter", "  - Item Type: ${currentItem.itemType}")

            holder.itemView.context.startActivity(intent)
        }

        when (currentItem.itemType.uppercase()) {
            "OBJECT" -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_objects)
                holder.itemTypeView.clearColorFilter()
                holder.itemTypeBadge.setBackgroundResource(R.drawable.badge_object)
                holder.itemType.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.colorPrimary
                    )
                )
                holder.iconContainer.setBackgroundResource(R.drawable.icon_gradient_object)
            }

            "CONTAINER" -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_container)
                holder.itemTypeView.clearColorFilter()
                holder.itemTypeBadge.setBackgroundResource(R.drawable.badge_container)
                holder.itemType.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.colorSecondary
                    )
                )
                holder.iconContainer.setBackgroundResource(R.drawable.icon_gradient_container)
            }

            else -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_block)
                holder.itemTypeView.clearColorFilter()

                holder.itemTypeBadge.setBackgroundResource(R.drawable.badge_background)
                holder.itemType.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.colorDarkGray
                    )
                )
            }
        }
        holder.createdTime.text = getFormattedTime(currentItem.createdAt)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun loadContainerInfo(itemId: String, containerNameView: TextView) {
        if (currentUser == null) return

        firestore.collection("user_inventories")
            .document(currentUser.uid)
            .collection("item_containers")
            .document(itemId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val containerName = document.getString("containerName")
                    if (!containerName.isNullOrEmpty()) {
                        containerNameView.text = "In: $containerName"
                        containerNameView.visibility = View.VISIBLE
                    } else {
                        containerNameView.visibility = View.GONE
                    }
                } else {
                    containerNameView.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e("ItemListAdapter", "Error loading container info: ${e.message}")
                containerNameView.visibility = View.GONE
            }
    }

    private fun getFormattedTime(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val now = Date()
            val diff = now.time - timestamp

            // If less than 24 hours, show relative time
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
                    // Show actual date for older items
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(date)
                }
            }
        } catch (e: Exception) {
            "Unknown time"
        }
    }
}