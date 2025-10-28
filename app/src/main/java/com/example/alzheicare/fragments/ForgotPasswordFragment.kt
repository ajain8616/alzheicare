package com.example.alzheicare.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alzheicare.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()

        arguments?.let { bundle ->
            val email = bundle.getString("email", "")
            if (email.isNotEmpty()) {
                binding.etEmailAddress.setText(email)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }

        binding.btnBackToLogin.setOnClickListener {
            goBackToLogin()
        }
    }

    private fun goBackToLogin() {
        if (activity != null) {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun resetPassword() {
        val email = binding.etEmailAddress.text.toString().trim()

        if (email.isBlank()) {
            showToast("Email cannot be blank")
            return
        }

        if (!isValidEmail(email)) {
            showToast("Please enter a valid email address")
            return
        }

        setLoadingState(true)
        checkEmailExistence(email)
    }

    private fun checkEmailExistence(email: String) {
        setLoadingState(true)

        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        setLoadingState(false)
                        showToast("No account found with this email address. Please sign up first.")
                        Log.d("ForgotPassword", "Email does not exist: $email")
                    } else {
                        sendPasswordResetEmail(email)
                    }
                } else {
                    setLoadingState(false)
                    showToast("Error checking email: ${task.exception?.message}")
                    Log.e("ForgotPassword", "Error checking email existence: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)
                showToast("Failed to check email: ${exception.message}")
                Log.e("ForgotPassword", "Failure checking email: ${exception.message}")
            }
    }

    private fun sendPasswordResetEmail(email: String) {
        setLoadingState(true)

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                setLoadingState(false)

                if (task.isSuccessful) {
                    showToast("Password reset link sent to $email")
                    Log.d("ForgotPassword", "Password reset email sent successfully to: $email")
                    binding.etEmailAddress.text?.clear()
                    goBackToLogin()
                } else {
                    val errorMessage = task.exception?.message ?: "Failed to send reset email"
                    showToast("Error: $errorMessage")
                    Log.e("ForgotPassword", "Error sending reset email: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { exception ->
                setLoadingState(false)
                showToast("Failed to send reset email: ${exception.message}")
                Log.e("ForgotPassword", "Failure: ${exception.message}")
            }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnResetPassword.isEnabled = !isLoading
        binding.btnResetPassword.text = if (isLoading) "Checking..." else "Send Reset Link"
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}