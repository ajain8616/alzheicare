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
import com.example.alzheicare.databinding.ActivityLoginIntroBinding
import com.example.alzheicare.databinding.DialogNoWifiBinding
import com.google.firebase.auth.FirebaseAuth

class LoginIntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginIntroBinding
    private lateinit var auth: FirebaseAuth
    private var noWifiDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        checkCurrentUser()
        setupClickListeners()
        checkInternetConnection()
        manualConnectionCheck()
    }

    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            if (isInternetAvailable()) {
                Toast.makeText(this, "User is already logged in!", Toast.LENGTH_SHORT).show()
                redirect("MAIN")
            } else {
                showNoWifiDialog()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            if (isInternetAvailable()) {
                redirect("LOGIN")
            } else {
                showNoWifiDialog()
            }
        }
    }

    private fun redirect(name: String) {
        val intent = when (name) {
            "LOGIN" -> Intent(this, LoginActivity::class.java)
            "MAIN" -> Intent(this, MainActivity::class.java)
            else -> throw Exception("No path exists")
        }
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null && isInternetAvailable()) {
            redirect("MAIN")
        }
    }

    override fun onResume() {
        super.onResume()
        checkInternetConnection()
    }

    override fun onStop() {
        super.onStop()
        noWifiDialog?.dismiss()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkInternetConnection() {
        if (!isInternetAvailable()) {
            showNoWifiDialog()
        } else {
            noWifiDialog?.dismiss()
            if (auth.currentUser != null) {
                binding.root.postDelayed({
                    redirect("MAIN")
                }, 500)
            }
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

        dialogBinding.imageCaption.text = "Internet connection required to continue. Please check your WiFi or mobile data and try again."
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
                Toast.makeText(this, "Internet connection required to use the app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retryAfterConnectionRestored() {
        if (auth.currentUser != null) {
            redirect("MAIN")
        }
    }

    fun manualConnectionCheck() {
        checkInternetConnection()
        Toast.makeText(this, "Checking connection...", Toast.LENGTH_SHORT).show()
    }
}