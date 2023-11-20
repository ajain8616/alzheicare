package com.example.itemsfinder

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var linearLayout:LinearLayout
    private lateinit var profile_picture:ImageView
    private lateinit var user_mail:TextView
    private lateinit var user_info:TextView
    private lateinit var logout_button:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        eventHandler()
    }

    private fun elementsId() {
        linearLayout=findViewById(R.id.linearLayout)
        profile_picture=findViewById(R.id.profile_picture)
        user_mail=findViewById(R.id.user_mail)
        user_info=findViewById(R.id.user_info)
        logout_button=findViewById(R.id.logout_button)
    }

    private fun eventHandler()
    {
        elementsId()
        val user = FirebaseAuth.getInstance().currentUser
        user_mail.text=user?.email
        user_info.text=user?.uid

        logout_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }
    }


}

