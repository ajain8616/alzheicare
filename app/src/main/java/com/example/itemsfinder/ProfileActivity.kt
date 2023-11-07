package com.example.itemsfinder


import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class ProfileActivity : AppCompatActivity() {
    private lateinit var profile_picture: ImageView
    private lateinit var user_info: TextView
    private lateinit var user_status: TextView
    private lateinit var logout_button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profile_picture = findViewById(R.id.profile_picture)
        user_info = findViewById(R.id.user_info)
        user_status = findViewById(R.id.user_status)
        logout_button = findViewById(R.id.logout_button)

    }
}
