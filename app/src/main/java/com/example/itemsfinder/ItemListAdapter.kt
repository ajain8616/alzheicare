package com.example.itemsfinder

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ItemListAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {
    private var onItemDeleteListener: OnItemDeleteListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ContainerDetailsActivity::class.java)
            context.startActivity(intent)
        }

        holder.updateImgBtn.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UpdateDetailsActivity::class.java)
            context.startActivity(intent)
        }


        holder.deleteImgBtn.setOnClickListener {
            val databaseReference = FirebaseDatabase.getInstance().getReference()
            val itemPath = "Item_Container_Details"
            databaseReference.child(itemPath).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Data successfully deleted from Firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(holder.itemView.context, "Error!!! Data did not delete from Firebase", Toast.LENGTH_SHORT).show()
                }
            onItemDeleteListener?.onItemDelete(item)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val itemTypeTextView: TextView = itemView.findViewById(R.id.itemTypeTextView)
        val updateImgBtn: ImageButton = itemView.findViewById(R.id.updateImgBtn)
        val deleteImgBtn: ImageButton = itemView.findViewById(R.id.deleteImgBtn)

        fun bind(item: Item) {
            itemNameTextView.text = item.name
            descriptionTextView.text = item.description
            itemTypeTextView.text = item.type
        }
    }
    interface OnItemDeleteListener {
        fun onItemDelete(item: Item)
    }
    fun setOnItemDeleteListener(listener: OnItemDeleteListener) {
        this.onItemDeleteListener = listener
    }

}
