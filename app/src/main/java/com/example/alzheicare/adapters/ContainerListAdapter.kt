package com.example.alzheicare.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.alzheicare.data_models.Item
import com.example.alzheicare.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ContainerListAdapter(
    private val containerList: List<Item>,
    private val listener: OnItemClickListener,
    private val firestore: FirebaseFirestore,
    private val userId: String
) : RecyclerView.Adapter<ContainerListAdapter.ContainerViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(container: Item)
    }

    private var selectedPosition = -1

    inner class ContainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val containerName: TextView = itemView.findViewById(R.id.containerName)
        val containerDescription: TextView = itemView.findViewById(R.id.containerDescription)
        val containerIcon: ImageView = itemView.findViewById(R.id.containerIcon)
        val containerBadge: TextView = itemView.findViewById(R.id.containerTypeBadge)
        val createdTime: TextView = itemView.findViewById(R.id.createdTime)
        val itemsCount: TextView = itemView.findViewById(R.id.itemsCount)
        val selectionCheckbox: CheckBox = itemView.findViewById(R.id.selectionCheckbox)
        val selectedOverlay: View = itemView.findViewById(R.id.selectedOverlay)
        val iconContainer: View = itemView.findViewById(R.id.iconContainer)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    listener.onItemClick(containerList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.container_item, parent, false)
        return ContainerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        val currentContainer = containerList[position]

        // Set container information
        holder.containerName.text = currentContainer.itemName
        holder.containerDescription.text = currentContainer.description
        holder.containerBadge.text = "CONTAINER"

        // Set created time
        holder.createdTime.text = getFormattedTime(currentContainer.createdAt)

        // Load items count for this container
        loadItemsCount(currentContainer.itemId, holder.itemsCount)

        // Set selection state
        val isSelected = position == selectedPosition
        holder.selectionCheckbox.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.selectionCheckbox.isChecked = isSelected

        // Style for container type
        holder.containerIcon.setImageResource(R.drawable.ic_container)
        holder.containerBadge.setBackgroundResource(R.drawable.badge_container)
        holder.iconContainer.setBackgroundResource(R.drawable.icon_gradient_container)
    }

    override fun getItemCount(): Int {
        return containerList.size
    }

    private fun setSelectedPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected)
        }
        notifyItemChanged(selectedPosition)
    }

    fun getSelectedContainer(): Item? {
        return if (selectedPosition != -1) containerList[selectedPosition] else null
    }

    private fun loadItemsCount(containerId: String, itemsCountView: TextView) {
        // Count items that have this container as their container
        firestore.collection("user_inventories")
            .document(userId)
            .collection("item_containers")
            .whereEqualTo("containerId", containerId)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                itemsCountView.text = "$count item${if (count != 1) "s" else ""}"
            }
            .addOnFailureListener { e ->
                Log.e("ContainerListAdapter", "Error loading items count: ${e.message}")
                itemsCountView.text = "0 items"
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

    companion object {
        private const val TAG = "ContainerListAdapter"
    }
}