package com.example.alzheicare.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.alzheicare.databinding.ActivitySignupBinding
import com.example.alzheicare.databinding.DialogNoWifiBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var noWifiDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupClickListeners()
        checkInternetConnection()
    }

    override fun onResume() {
        super.onResume()
        checkInternetConnection()
    }

    override fun onStop() {
        super.onStop()
        noWifiDialog?.dismiss()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            if (isInternetAvailable()) {
                signUpUser()
            } else {
                showNoWifiDialog()
            }
        }

        binding.btnLogin.setOnClickListener {
            if (isInternetAvailable()) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                showNoWifiDialog()
            }
        }
    }

    private fun signUpUser() {
        val email = binding.etEmailAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isInternetAvailable()) {
            showNoWifiDialog()
            return
        }

        setLoadingState(true)

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoadingState(false)

                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Verification link sent to your email. Please verify your email before logging in.",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Error sending verification email: ${verificationTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error creating user"
                    Toast.makeText(this, "Signup failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)
                Toast.makeText(this, "Signup error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnSignUp.isEnabled = !isLoading
        binding.btnSignUp.text = if (isLoading) "Creating Account..." else "Sign Up"
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkInternetConnection() {
        if (!isInternetAvailable()) {
            showNoWifiDialog()
        } else {
            noWifiDialog?.dismiss()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun showNoWifiDialog() {
        noWifiDialog?.dismiss()

        val dialogBinding = DialogNoWifiBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            .setView(dialogBinding.root)
            .setCancelable(false)

        noWifiDialog = dialogBuilder.create()
        noWifiDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        noWifiDialog?.show()

        dialogBinding.imageCaption.text = "Internet connection required for signup. Please check your WiFi or mobile data and try again."
        dialogBinding.customButton.text = "Try Again"

        dialogBinding.customButton.setOnClickListener {
            if (isInternetAvailable()) {
                noWifiDialog?.dismiss()
                retryAfterConnectionRestored()
            } else {
                Toast.makeText(this, "Still no internet connection. Please check your WiFi or mobile data.", Toast.LENGTH_SHORT).show()
            }
        }

        noWifiDialog?.setOnDismissListener {
            if (!isInternetAvailable()) {
                Toast.makeText(this, "Internet connection required to signup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retryAfterConnectionRestored() {
        if (isInternetAvailable()) {
            Toast.makeText(this, "Internet connection restored. You can now sign up.", Toast.LENGTH_SHORT).show()
        }
    }
}