package com.example.itemsfinder

import android.animation.Animator
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DataSetDetailsActivity : AppCompatActivity() {
    private lateinit var itemName: TextView
    private lateinit var description: TextView
    private lateinit var itemType: TextView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var editActionButton: FloatingActionButton
    private lateinit var deleteActionButton: FloatingActionButton
    private lateinit var cardView:CardView
    private lateinit var auth: FirebaseAuth
    private lateinit var deleteMessageAnimation:LottieAnimationView
    private lateinit var containerMessage:LottieAnimationView
    private lateinit var containerView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_set_details)
        auth = FirebaseAuth.getInstance()
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

        containerView.setOnClickListener {
            containerActivityAnimation()
        }

        deleteActionButton.setOnClickListener {
            val itemName = itemName.text
            deleteDataFromFirebase(itemName.toString())
            deleteMessageAnimation.visibility = View.VISIBLE
            editActionButton.visibility = View.GONE
            deleteActionButton.visibility = View.GONE
            cardView.visibility = View.GONE
            floatingActionButton.visibility = View.GONE
            editActionButton.visibility = View.GONE
            Handler(Looper.getMainLooper()).postDelayed({
                deleteMessageAnimation.visibility = View.VISIBLE
                val intent = Intent(this@DataSetDetailsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            },2000)

        }


    }
        private fun findIdsOfElements() {
            itemName = findViewById(R.id.itemNameView)
            description = findViewById(R.id.descriptionView)
            itemType = findViewById(R.id.itemTypeView)
            floatingActionButton = findViewById(R.id.floatingActionButton)
            editActionButton = findViewById(R.id.editActionButton)
            deleteActionButton = findViewById(R.id.deleteActionButton)
            cardView=findViewById(R.id.cardView)
            deleteMessageAnimation=findViewById(R.id.deleteMessage)
            containerMessage=findViewById(R.id.containerMessage)
            containerView=findViewById(R.id.containerView)
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
    private fun deleteDataFromFirebase(itemId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val collectionName = "Item_Container_Data"
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = database.child(collectionName).child(userId).child(itemId)
            userRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    deleteMessageAnimation.playAnimation()
                } else {
                    Toast.makeText(this, "Failed to delete item $itemId", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
    private fun containerActivityAnimation() {
        containerMessage.visibility=View.VISIBLE
        deleteMessageAnimation.visibility = View.GONE
        editActionButton.visibility = View.GONE
        deleteActionButton.visibility = View.GONE
        cardView.visibility = View.GONE
        containerView.visibility=View.GONE
        floatingActionButton.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            containerMessage.visibility=View.VISIBLE
            val intent = Intent(this@DataSetDetailsActivity, ContainerChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
