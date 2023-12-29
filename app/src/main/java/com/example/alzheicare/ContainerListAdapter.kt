package com.example.alzheicare

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ContainerListAdapter(
    private val containerList: List<Item>,
    private val itemClickListener: OnItemClickListener,
    private val itemLongClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<ContainerListAdapter.ContainerViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(container: Item, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(container: Item): Boolean
    }

    private var lastCheckedPosition: Int = -1

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
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val containerSelectedView: TextView =
            itemView.findViewById(R.id.containerSelectedView)

        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val container = containerList[position]

                    if (lastCheckedPosition != -1) {
                        containerList[lastCheckedPosition].isChecked = false
                        notifyItemChanged(lastCheckedPosition)
                    }
                    container.isChecked = true
                    notifyItemChanged(position)
                    lastCheckedPosition = position

                    itemLongClickListener.onItemLongClick(container)

                    true
                } else {
                    false
                }
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val container = containerList[position]
                    itemClickListener.onItemClick(container, position)
                }
            }
        }

        fun bind(container: Item) {
            if (!container.isChecked) {
                val colorPicker = ColorPicker.getColor()
                cardContainer.setCardBackgroundColor(Color.parseColor(colorPicker))
            }

            if (container.itemType == "CONTAINER") {
                containerSelectedView.text = container.itemName
            } else {
                containerSelectedView.text = ""
            }

            checkBox.isChecked = container.isChecked
            checkBox.visibility = if (container.isChecked) View.VISIBLE else View.GONE
        }

    }
}