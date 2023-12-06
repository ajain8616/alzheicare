package com.example.alzheicare

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class ContainerListAdapter(
    private val containerList: List<Item>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ContainerListAdapter.ContainerViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(container: Item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.container_item, parent, false)
        return ContainerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        val container = containerList[position]
        holder.bind(container)
    }

    override fun getItemCount(): Int {
        return containerList.size
    }

    inner class ContainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardContainer: CardView = itemView.findViewById(R.id.cardContainer)
        private val containerIcon: ImageView = itemView.findViewById(R.id.containerIcon)
        private val containerSelectedView: TextView =
            itemView.findViewById(R.id.containerSelectedView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val container = containerList[position]
                    itemClickListener.onItemClick(container)
                }
            }
        }

        fun bind(container: Item) {
            // Set data to views

            // Set the icon using IconPicker class
            val iconPicker = IconPicker.getIcon()
            containerIcon.setImageResource(iconPicker)

            // Set the background color using ColorPicker class
            val colorPicker = ColorPicker.getColor()
            cardContainer.setCardBackgroundColor(Color.parseColor(colorPicker))

            // Set the text in containerSelectedView only when itemType is "CONTAINER"
            if (container.itemType == "CONTAINER") {
                // Append container names to containerSelectedView
                val currentText = containerSelectedView.text.toString()
                val containerNames = if (currentText.isNotEmpty()) {
                    "$currentText, ${container.itemName}"
                } else {
                    container.itemName
                }
                containerSelectedView.text = containerNames
            } else {
                containerSelectedView.text = ""
            }
        }
    }
}
