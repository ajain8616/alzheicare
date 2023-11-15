package com.example.itemsfinder

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ItemListAdapter(private val itemList: List<Item>) :
    RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val description: TextView = itemView.findViewById(R.id.description)
        val itemType: TextView = itemView.findViewById(R.id.itemType)
        val itemTypeView:ImageView=itemView.findViewById(R.id.itemTypeView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.itemName.text = currentItem.itemName
        holder.description.text = currentItem.description
        holder.itemType.text = currentItem.itemType

        val context = holder.itemView.context

        when (currentItem.itemType) {
            "OBJECT" -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_items)
                holder.itemTypeView.setColorFilter(ContextCompat.getColor(context, R.color.colorBlue))
                holder.itemView.setBackgroundResource(R.drawable.item_view_border_blue)
            }
            "CONTAINER" -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_containers)
                holder.itemTypeView.setColorFilter(ContextCompat.getColor(context, R.color.colorDarkGray))
                holder.itemView.setBackgroundResource(R.drawable.item_view_border_gray)
            }
            else -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_block)
                holder.itemTypeView.clearColorFilter()
                holder.itemView.background = null
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ContainerDetailsActivity::class.java)
            intent.putExtra("itemName", currentItem.itemName)
            intent.putExtra("description", currentItem.description)
            intent.putExtra("itemType", currentItem.itemType)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}
