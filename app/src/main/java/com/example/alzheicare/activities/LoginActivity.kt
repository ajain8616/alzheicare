package com.example.alzheicare.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.alzheicare.databinding.ActivityLoginBinding
import com.example.alzheicare.databinding.DialogNoWifiBinding
import com.example.alzheicare.fragments.ForgotPasswordFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var countDownTimer: CountDownTimer? = null
    private var isResendEnabled = true
    private val RESEND_DELAY = 2 * 60 * 1000L
    private var noWifiDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser

        setupClickListeners()
        checkInternetConnection()

        if (currentUser != null && currentUser!!.isEmailVerified) {
            redirectToMain()
        } else if (currentUser != null && !currentUser!!.isEmailVerified) {
            showEmailNotVerifiedUI()
        }
    }

    override fun onResume() {
        super.onResume()
        checkInternetConnection()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        noWifiDialog?.dismiss()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (isInternetAvailable()) {
                login()
            } else {
                showNoWifiDialog()
            }
        }

        binding.btnForgetPassword.setOnClickListener {
            if (isInternetAvailable()) {
                showForgotPasswordFragment()
            } else {
                showNoWifiDialog()
            }
        }

        binding.btnSignUp.setOnClickListener {
            if (isInternetAvailable()) {
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                showNoWifiDialog()
            }
        }

        binding.btnResendVerification.setOnClickListener {
            if (isResendEnabled) {
                if (isInternetAvailable()) {
                    resendVerificationEmail()
                } else {
                    showNoWifiDialog()
                }
            }
        }
    }

    private fun showForgotPasswordFragment() {
        val fragment = ForgotPasswordFragment().apply {
            arguments = Bundle().apply {
                putString("email", binding.etEmailAddress.text.toString().trim())
            }
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(android.R.id.content, fragment)
        transaction.addToBackStack("forgot_password")
        transaction.commit()
    }

    private fun login() {
        val email = binding.etEmailAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isInternetAvailable()) {
            showNoWifiDialog()
            return
        }

        setLoadingState(true)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    currentUser = firebaseAuth.currentUser
                    checkEmailVerification()
                } else {
                    setLoadingState(false)
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(this, "Login Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)
                Toast.makeText(this, "Login Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkEmailVerification() {
        currentUser?.let { user ->
            user.reload().addOnCompleteListener { reloadTask ->
                setLoadingState(false)

                if (reloadTask.isSuccessful) {
                    if (user.isEmailVerified) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        redirectToMain()
                    } else {
                        showEmailNotVerifiedUI()
                        Toast.makeText(
                            this,
                            "Please verify your email address to continue",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error checking email verification",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showEmailNotVerifiedUI() {
        binding.txtVerificationInfo.visibility = android.view.View.VISIBLE
        binding.btnResendVerification.visibility = android.view.View.VISIBLE
        startResendTimer()
    }

    private fun resendVerificationEmail() {
        currentUser?.let { user ->
            setLoadingState(true, "Sending...")

            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    setLoadingState(false, "Log In")

                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Verification link sent to ${user.email}",
                            Toast.LENGTH_LONG
                        ).show()
                        startResendTimer()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to send verification email: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    setLoadingState(false, "Log In")
                    Toast.makeText(
                        this,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: run {
            Toast.makeText(
                this,
                "Please login first to resend verification email",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startResendTimer() {
        isResendEnabled = false
        binding.btnResendVerification.isEnabled = false
        binding.btnResendVerification.text = "Wait 2:00"

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(RESEND_DELAY, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.btnResendVerification.text =
                    String.format("Wait %d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                isResendEnabled = true
                binding.btnResendVerification.isEnabled = true
                binding.btnResendVerification.text = "Resend Verification Link"
            }
        }.start()
    }

    private fun setLoadingState(isLoading: Boolean, buttonText: String = "Signing In...") {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) buttonText else "Log In"
        binding.btnResendVerification.isEnabled = !isLoading && isResendEnabled
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
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
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)

        noWifiDialog = builder.create()
        noWifiDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        noWifiDialog?.show()

        // Modified: Open WiFi settings when action button is clicked
        dialogBinding.customButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
            noWifiDialog?.dismiss()
            retryAfterConnectionRestored()

        }

        // Close dialog when cross icon is clicked
        dialogBinding.crossIcon.setOnClickListener {
            noWifiDialog?.dismiss()
            retryAfterConnectionRestored()

        }
    }

    private fun retryAfterConnectionRestored() {
        if (currentUser != null && currentUser!!.isEmailVerified) {
            redirectToMain()
        }
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}