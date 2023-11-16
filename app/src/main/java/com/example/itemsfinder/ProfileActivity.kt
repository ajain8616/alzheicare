package com.example.itemsfinder


import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var profile_picture: ImageView
    private lateinit var user_info: TextView
    private lateinit var user_status: TextView
    private lateinit var logout_button: ImageButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profile_picture = findViewById(R.id.profile_picture)
        user_info = findViewById(R.id.user_info)
        user_status = findViewById(R.id.user_status)
        logout_button = findViewById(R.id.logout_button)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.reference.child("users")

        // Get the current user's email ID
        val currentUser = mAuth.currentUser
        val email = currentUser?.email

        // Set the email ID in the user_info TextView
        user_info.text = email

        // Set the user's status in the user_status TextView
        val status = getUserStatus()
        user_status.text = status

        // Push the user's status and email to Firebase
        pushUserStatus(status, email)


        logout_button.setOnClickListener {
            logout()
        }
    }

    private fun getUserStatus(): String {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return when {
            networkInfo == null -> "Offline"
            networkInfo.isConnected -> "Online"
            else -> "Working"
        }
    }

    private fun pushUserStatus(status: String, email: String?) {
        val currentUser = mAuth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            userRef.child("status").setValue(status)
            userRef.child("email").setValue(email)
        }
    }

    private fun logout() {
        mAuth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
