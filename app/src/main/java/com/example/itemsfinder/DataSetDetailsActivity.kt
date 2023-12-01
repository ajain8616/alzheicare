package com.example.itemsfinder

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
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
    private lateinit var cardView: CardView
    private lateinit var auth: FirebaseAuth
    private lateinit var deleteMessageAnimation: LottieAnimationView
    private lateinit var containerMessage: LottieAnimationView
    private lateinit var containerView: TextView
    private lateinit var editItemName: EditText
    private lateinit var editDescription: EditText
    private lateinit var radioGroupItemType: RadioGroup
    private lateinit var radioItem: RadioButton
    private lateinit var radioContainer: RadioButton
    private lateinit var saveChangesButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var updateMessage: LottieAnimationView
    private lateinit var linearLayoutForm: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_set_details)
        auth = FirebaseAuth.getInstance()
        setEventHandlers()
        displayData()
    }

    private fun setEventHandlers() {
        findIdsOfElements()
        linearLayoutForm.visibility = View.GONE
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
            linearLayoutForm.visibility = View.VISIBLE
            editActionButton.visibility = View.GONE
            deleteActionButton.visibility = View.GONE
            cardView.visibility = View.GONE
            floatingActionButton.visibility = View.GONE
            editActionButton.visibility = View.GONE
            containerView.visibility = View.GONE
            updateMessage.visibility = View.GONE
            backButton.visibility = View.VISIBLE

        }

        backButton.setOnClickListener {
            linearLayoutForm.visibility = View.GONE
            editActionButton.visibility = View.VISIBLE
            deleteActionButton.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            floatingActionButton.visibility = View.VISIBLE
            editActionButton.visibility = View.VISIBLE
            containerView.visibility = View.VISIBLE
            updateMessage.visibility = View.VISIBLE

        }

        saveChangesButton.setOnClickListener {
            updateDetails()
            updateMessage.visibility = View.VISIBLE
            editActionButton.visibility = View.GONE
            deleteActionButton.visibility = View.GONE
            cardView.visibility = View.GONE
            floatingActionButton.visibility = View.GONE
            editActionButton.visibility = View.GONE
            containerView.visibility = View.GONE
            linearLayoutForm.visibility = View.GONE
            deleteMessageAnimation.visibility = View.GONE
            backButton.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                updateMessage.visibility = View.VISIBLE
                val intent = Intent(this@DataSetDetailsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000)

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
            containerView.visibility = View.GONE
            linearLayoutForm.visibility = View.GONE
            updateMessage.visibility = View.GONE
            backButton.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                deleteMessageAnimation.visibility = View.VISIBLE
                val intent = Intent(this@DataSetDetailsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000)

        }

        containerView.setOnClickListener {
            containerActivityAnimation()
            containerMessage.playAnimation()
        }

    }

    private fun findIdsOfElements() {
        itemName = findViewById(R.id.itemNameView)
        description = findViewById(R.id.descriptionView)
        itemType = findViewById(R.id.itemTypeView)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        editActionButton = findViewById(R.id.editActionButton)
        deleteActionButton = findViewById(R.id.deleteActionButton)
        cardView = findViewById(R.id.cardView)
        deleteMessageAnimation = findViewById(R.id.deleteMessage)
        containerMessage = findViewById(R.id.containerMessage)
        containerView = findViewById(R.id.containerView)
        editItemName = findViewById(R.id.editItemName)
        editDescription = findViewById(R.id.editDescription)
        radioGroupItemType = findViewById(R.id.radioGroupItemType)
        radioItem = findViewById(R.id.radioItem)
        radioContainer = findViewById(R.id.radioContainer)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        updateMessage = findViewById(R.id.updateMessage)
        linearLayoutForm = findViewById(R.id.linearLayoutForm)
        backButton = findViewById(R.id.backButton)
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
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_LONG).show()
        }
    }

    private fun containerActivityAnimation() {
        containerMessage.visibility = View.VISIBLE
        deleteMessageAnimation.visibility = View.GONE
        editActionButton.visibility = View.GONE
        deleteActionButton.visibility = View.GONE
        cardView.visibility = View.GONE
        containerView.visibility = View.GONE
        floatingActionButton.visibility = View.GONE
        linearLayoutForm.visibility = View.GONE
        updateMessage.visibility = View.GONE
        backButton.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            containerMessage.visibility = View.VISIBLE
            val intent = Intent(this@DataSetDetailsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun updateDetails() {
        val itemNameExtra = itemName.text.toString()
        val updatedDescription = editDescription.text.toString()

        // Check if updatedDescription is not empty and a radio button is selected
        if (updatedDescription.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance().reference
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val selectedRadioButtonId = radioGroupItemType.checkedRadioButtonId

            // Check if a radio button is selected
            if (selectedRadioButtonId != -1 && userId != null) {
                // Determine the collection name based on the selected radio button
                val collectionName = when (selectedRadioButtonId) {
                    R.id.radioItem -> "Item_Container_Data_Item"
                    R.id.radioContainer -> "Item_Container_Data_Container"
                    else -> "Item_Container_Data" // Default collection name if none selected
                }
                val userRef = database.child(collectionName).child(userId).child(itemNameExtra)
                // Get the selected item type from the radio button
                val updatedItemType = when (selectedRadioButtonId) {
                    R.id.radioItem -> "Item"
                    R.id.radioContainer -> "Container"
                    else -> "" // Default item type if none selected
                }

                // Create a map with the updated data
                val updatedData = mapOf(
                    "description" to updatedDescription,
                    "itemType" to updatedItemType
                )

                // Update the data in Firebase
                userRef.updateChildren(updatedData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Update UI or show a success message
                            updateMessage.playAnimation()
                            description.text = updatedDescription
                            itemType.text = updatedItemType
                        } else {
                            Toast.makeText(
                                this@DataSetDetailsActivity,
                                "Failed to update item $itemNameExtra",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this@DataSetDetailsActivity,
                    "Please select an item type",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this@DataSetDetailsActivity,
                "Please enter values for all fields",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
