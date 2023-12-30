package com.example.alzheicare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContainerCollectionAdapter(private val context: Context, private val containerCollectionList: List<Item>) : RecyclerView.Adapter<ContainerCollectionAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.container_collecection, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = containerCollectionList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return containerCollectionList.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val selectedContainerName: TextView = itemView.findViewById(R.id.selectedContainerName)
        fun bind(item: Item) {
            selectedContainerName.text = item.itemName
        }
    }
}
