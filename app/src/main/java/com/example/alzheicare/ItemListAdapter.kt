package com.example.alzheicare

import android.content.Intent
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
        val itemTypeView: ImageView = itemView.findViewById(R.id.itemTypeView)
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

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DataSetDetailsActivity::class.java)
            intent.putExtra("itemName", currentItem.itemName)
            intent.putExtra("description", currentItem.description)
            intent.putExtra("itemType", currentItem.itemType)
            holder.itemView.context.startActivity(intent)
        }


        when (currentItem.itemType) {
            "OBJECT" -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_objects)
                holder.itemTypeView.setColorFilter(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.colorBlue
                    )
                )
                holder.itemView.setBackgroundResource(R.drawable.item_view_border_blue)
            }

            "CONTAINER" -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_container)
                holder.itemTypeView.setColorFilter(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.colorDarkGray
                    )
                )
                holder.itemView.setBackgroundResource(R.drawable.item_view_border_gray)
            }

            else -> {
                holder.itemTypeView.setImageResource(R.drawable.ic_block)
                holder.itemTypeView.setColorFilter(null)
                holder.itemView.background = null
            }

        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}