package com.example.alzheicare

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: TextView
    private lateinit var btnForgetPassword: TextView
    private lateinit var etEmailAddress: EditText
    private lateinit var etPassword: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        firebaseAuth = FirebaseAuth.getInstance()
        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            login()
        }
        btnForgetPassword = findViewById(R.id.btnForgetPassword)
        btnForgetPassword.setOnClickListener {
            forgetPassword()
        }

        btnSignUp = findViewById(R.id.btnSignUp)
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
        checkInternetConnection()

    }

    private fun login() {
        etEmailAddress = findViewById(R.id.etEmailAddress)
        etPassword = findViewById(R.id.etPassword)
        val email = etEmailAddress.text.toString()
        val password = etPassword.text.toString()
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun forgetPassword() {
        val email = etEmailAddress.text.toString()

        if (email.isBlank()) {
            Toast.makeText(this, "Email cannot be blank", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset link sent to your email.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Error sending password reset email.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
    private fun checkInternetConnection() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected

        if (isConnected) {
            Toast.makeText(
                this,
                "Your internet is turned on. Now you can use the app.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Your internet is turned off. Please turn on your internet for using the app.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
