package com.example.alzheicare

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class SignupActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnLogin: TextView
    private lateinit var btnSignUp: Button
    private lateinit var etEmailAddress: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        firebaseAuth = FirebaseAuth.getInstance()
        btnSignUp = findViewById(R.id.btnSignUp)
        btnSignUp.setOnClickListener {
            signUpUser()
        }

        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        checkInternetConnection()
    }

    private fun signUpUser() {
        etEmailAddress = findViewById(R.id.etEmailAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        val email = etEmailAddress.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT)
                .show()
            return
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Verification link sent to your email.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Error sending verification email.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Error creating user.", Toast.LENGTH_SHORT).show()
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
