package com.example.itemsfinder

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataSetDetailsActivity : AppCompatActivity() {
    private lateinit var itemName: TextView
    private lateinit var description: TextView
    private lateinit var itemType: TextView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var editActionButton: FloatingActionButton
    private lateinit var deleteActionButton: FloatingActionButton
    private lateinit var cardView:CardView
    private lateinit var containerAnimation:LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_set_details)
        setEventHandlers()
        displayData()

    }

    private fun setEventHandlers() {
        findIdsOfElements()
        floatingActionButton.setOnClickListener {
            if (editActionButton.visibility == View.GONE || deleteActionButton.visibility == View.GONE) {
                editActionButton.visibility = View.VISIBLE
                deleteActionButton.visibility = View.VISIBLE
            } else {
                editActionButton.visibility = View.GONE
                deleteActionButton.visibility = View.GONE
            }
        }

        editActionButton.setOnClickListener {
            val intent = Intent(this, UpdateDetailsActivity::class.java)
            startActivity(intent)
        }

        deleteActionButton.setOnClickListener {

        }

        containerAnimation.setOnClickListener {
            containerAnimation.playAnimation()
            startContainerAnimationWithDelay()
        }
    }
        private fun startContainerAnimationWithDelay() {
            containerAnimation.visibility = View.INVISIBLE // Initially make the animation view invisible
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                containerAnimation.visibility = View.VISIBLE // Make the animation view visible before starting the animation

                val intent = Intent(this, ContainerChoiceActivity::class.java)
                startActivity(intent)
            }, 3000)
        }



        private fun findIdsOfElements() {
        itemName = findViewById(R.id.itemNameView)
        description = findViewById(R.id.descriptionView)
        itemType = findViewById(R.id.itemTypeView)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        editActionButton = findViewById(R.id.editActionButton)
        deleteActionButton = findViewById(R.id.deleteActionButton)
        cardView=findViewById(R.id.cardView)
        containerAnimation=findViewById(R.id.containerAnimation)
    }

    private fun displayData() {
        val itemNameExtra = intent.getStringExtra("itemName")
        val descriptionExtra = intent.getStringExtra("description")
        val itemTypeExtra = intent.getStringExtra("itemType")
        itemName.text = itemNameExtra
        itemName.setTypeface(null, Typeface.BOLD)
        description.text = descriptionExtra
        itemType.text = itemTypeExtra

        when (itemTypeExtra) {
            "OBJECT" -> {
                itemType.setTextColor(ContextCompat.getColor(this, R.color.colorBlue))
                findViewById<View>(R.id.cardView).setBackgroundResource(R.drawable.item_view_border_blue)
            }
            "CONTAINER" -> {
                itemType.setTextColor(ContextCompat.getColor(this, R.color.colorDarkGray))
                findViewById<View>(R.id.cardView).setBackgroundResource(R.drawable.item_view_border_gray)
            }
        }
    }

}
